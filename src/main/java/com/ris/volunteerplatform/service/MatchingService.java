package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.RecommendedVolunteerDto;
import com.ris.volunteerplatform.entity.*;
import com.ris.volunteerplatform.entity.ApplicationStatus;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.*;
import com.ris.volunteerplatform.security.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);
    private static final BigDecimal MAX_PROF = BigDecimal.valueOf(5);

    private final TaskRepository taskRepository;
    private final TaskRequirementRepository taskRequirementRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerSkillRepository volunteerSkillRepository;
    private final MatchResultRepository matchResultRepository;
    private final TaskApplicationRepository taskApplicationRepository;

    public void authorizeTaskRanking(Long taskId, Authentication authentication) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + taskId));
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUserDetails sud)) {
            throw new AccessDeniedException("Требуется аутентификация");
        }
        User user = sud.getUser();
        if (user.getRole() == UserRole.COORDINATOR || user.getRole() == UserRole.ADMIN) {
            return;
        }
        if (user.getRole() == UserRole.ORGANIZER && task.getOrganizer().getId().equals(user.getId())) {
            return;
        }
        throw new AccessDeniedException("Недостаточно прав для матчинга по этой задаче");
    }

    @Transactional
    public List<RecommendedVolunteerDto> computeAndPersistRankings(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + taskId));

        matchResultRepository.deleteByTaskId(taskId);

        // Берём всех кто откликнулся (PENDING или APPROVED)
        Set<Long> applicantIds = taskApplicationRepository.findByTaskId(taskId).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING
                        || a.getStatus() == ApplicationStatus.APPROVED)
                .map(a -> a.getVolunteer().getId())
                .collect(Collectors.toSet());

        if (applicantIds.isEmpty()) {
            return List.of();
        }

        List<Volunteer> candidates = volunteerRepository.findAllById(applicantIds);

        List<TaskRequirement> requirements = taskRequirementRepository.findByTaskId(taskId);

        // Если требований нет — показываем всех кандидатов со score 0
        if (requirements.isEmpty()) {
            List<MatchResult> batch = candidates.stream()
                    .map(v -> MatchResult.builder().task(task).volunteer(v).matchScore(BigDecimal.ZERO).build())
                    .toList();
            matchResultRepository.saveAll(batch);
            return candidates.stream()
                    .map(v -> new RecommendedVolunteerDto(v.getId(), v.getUser().getId(),
                            v.getFullName(), BigDecimal.ZERO))
                    .toList();
        }

        BigDecimal totalWeight = requirements.stream()
                .map(TaskRequirement::getImportanceWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        List<MatchResult> batch = new ArrayList<>();
        List<RecommendedVolunteerDto> ranked = new ArrayList<>();

        for (Volunteer volunteer : candidates) {
            Map<Long, Integer> skillToProficiency = loadVolunteerSkillMap(volunteer.getId());
            BigDecimal earned = BigDecimal.ZERO;

            for (TaskRequirement req : requirements) {
                Long skillId = req.getSkill().getId();
                if (skillToProficiency.containsKey(skillId)) {
                    BigDecimal profFactor = BigDecimal.valueOf(skillToProficiency.get(skillId))
                            .divide(MAX_PROF, MC);
                    earned = earned.add(req.getImportanceWeight().multiply(profFactor, MC));
                }
            }

            BigDecimal score = earned.divide(totalWeight, 6, RoundingMode.HALF_UP);
            batch.add(MatchResult.builder().task(task).volunteer(volunteer).matchScore(score).build());
            ranked.add(new RecommendedVolunteerDto(
                    volunteer.getId(), volunteer.getUser().getId(), volunteer.getFullName(), score));
        }

        matchResultRepository.saveAll(batch);
        ranked.sort(Comparator.comparing(RecommendedVolunteerDto::matchScore).reversed());
        return ranked;
    }

    private Map<Long, Integer> loadVolunteerSkillMap(Long volunteerId) {
        return volunteerSkillRepository.findByVolunteerId(volunteerId).stream()
                .collect(Collectors.toMap(
                        vs -> vs.getSkill().getId(),
                        VolunteerSkill::getProficiencyLevel,
                        Math::max));
    }
}
