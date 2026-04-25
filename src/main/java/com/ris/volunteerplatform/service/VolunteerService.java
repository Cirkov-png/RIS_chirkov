package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.*;
import com.ris.volunteerplatform.entity.*;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final UserRepository userRepository;
    private final TaskApplicationRepository taskApplicationRepository;
    private final TaskRepository taskRepository;

    /** UC1: Все волонтёры */
    @Transactional(readOnly = true)
    public List<VolunteerDto> findAll() {
        return volunteerRepository.findAll().stream().map(this::toDto).toList();
    }

    /** UC2: Только активные */
    @Transactional(readOnly = true)
    public List<VolunteerDto> findAllActive() {
        return volunteerRepository.findByActiveTrue().stream().map(this::toDto).toList();
    }

    /** UC3: Профиль по id */
    @Transactional(readOnly = true)
    public VolunteerDto findById(Long id) {
        return volunteerRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + id));
    }

    /** UC4: Профиль по userId */
    @Transactional(readOnly = true)
    public VolunteerDto findByUserId(Long userId) {
        return volunteerRepository.findByUser_Id(userId).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль волонтёра не найден для пользователя: " + userId));
    }

    /** UC5: Создание профиля */
    @Transactional
    public VolunteerDto create(VolunteerRequest req) {
        var user = userRepository.findById(req.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + req.userId()));
        if (user.getRole() != UserRole.VOLUNTEER) {
            throw new BadRequestException("Профиль волонтёра можно связать только с пользователем роли VOLUNTEER");
        }
        if (volunteerRepository.findByUser_Id(req.userId()).isPresent()) {
            throw new BadRequestException("У пользователя уже есть профиль волонтёра");
        }
        Volunteer v = Volunteer.builder()
                .user(user)
                .fullName(req.fullName())
                .phone(req.phone())
                .region(req.region())
                .bio(req.bio())
                .active(req.active() != null ? req.active() : true)
                .birthDate(req.birthDate())
                .avatarUrl(req.avatarUrl())
                .rating(BigDecimal.ZERO)
                .completedTasksCount(0)
                .manualRatingSum(BigDecimal.ZERO)
                .manualRatingCount(0)
                .build();
        return toDto(volunteerRepository.save(v));
    }

    /** UC6: Редактирование профиля */
    @Transactional
    public VolunteerDto update(Long id, VolunteerRequest req) {
        Volunteer v = volunteerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + id));
        if (!v.getUser().getId().equals(req.userId())) {
            throw new BadRequestException("Смена пользователя через update не поддерживается");
        }
        v.setFullName(req.fullName());
        v.setPhone(req.phone());
        v.setRegion(req.region());
        v.setBio(req.bio());
        v.setBirthDate(req.birthDate());
        v.setAvatarUrl(req.avatarUrl());
        if (req.active() != null) v.setActive(req.active());
        return toDto(volunteerRepository.save(v));
    }

    /** UC7: Деактивация */
    @Transactional
    public VolunteerDto deactivate(Long id) {
        Volunteer v = volunteerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + id));
        v.setActive(false);
        return toDto(volunteerRepository.save(v));
    }

    /** UC8: Активация */
    @Transactional
    public VolunteerDto activate(Long id) {
        Volunteer v = volunteerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + id));
        v.setActive(true);
        return toDto(volunteerRepository.save(v));
    }

    /** UC9: Мои заявки */
    @Transactional(readOnly = true)
    public List<TaskApplicationDto> getMyApplications(Long volunteerId) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        return taskApplicationRepository.findByVolunteer(v).stream()
                .map(this::toApplicationDto).toList();
    }

    /** UC10: Отзыв заявки */
    @Transactional
    public TaskApplicationDto withdrawApplication(Long volunteerId, Long applicationId) {
        TaskApplication app = taskApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + applicationId));
        if (!app.getVolunteer().getId().equals(volunteerId)) {
            throw new BadRequestException("Это не ваша заявка");
        }
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Отозвать можно только заявку в статусе PENDING");
        }
        app.setStatus(ApplicationStatus.WITHDRAWN);
        return toApplicationDto(taskApplicationRepository.save(app));
    }

    /** UC11: Одобренные и завершённые по заявкам задачи волонтёра */
    @Transactional(readOnly = true)
    public List<TaskDto> getMyApprovedTasks(Long volunteerId) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        var statuses = EnumSet.of(
                ApplicationStatus.APPROVED,
                ApplicationStatus.COMPLETED_SUCCESS,
                ApplicationStatus.COMPLETED_FAILURE);
        Map<Long, Task> byTaskId = new LinkedHashMap<>();
        for (TaskApplication app : taskApplicationRepository.findByVolunteerAndStatusIn(v, statuses)) {
            byTaskId.putIfAbsent(app.getTask().getId(), app.getTask());
        }
        return byTaskId.values().stream().map(this::toTaskDto).toList();
    }

    /** UC12: Удаление профиля */
    @Transactional
    public void delete(Long id) {
        if (!volunteerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Волонтёр не найден: " + id);
        }
        volunteerRepository.deleteById(id);
    }

    /** UC13: Поиск по региону */
    @Transactional(readOnly = true)
    public List<VolunteerDto> findByRegion(String region, boolean onlyActive) {
        List<Volunteer> result = onlyActive
                ? volunteerRepository.findByActiveTrueAndRegionIgnoreCase(region)
                : volunteerRepository.findByRegionIgnoreCase(region);
        return result.stream().map(this::toDto).toList();
    }

    /** UC14: Поиск по навыку */
    @Transactional(readOnly = true)
    public List<VolunteerDto> findBySkill(Long skillId, boolean onlyActive) {
        List<Volunteer> result = onlyActive
                ? volunteerRepository.findActiveBySkillId(skillId)
                : volunteerRepository.findBySkillId(skillId);
        return result.stream().map(this::toDto).toList();
    }

    /** UC15: Ручная оценка координатора (учитывается в среднем вместе с оценками по закрытым заявкам) */
    @Transactional
    public VolunteerDto rateVolunteer(Long id, BigDecimal newRating) {
        if (newRating.compareTo(BigDecimal.ZERO) < 0 || newRating.compareTo(new BigDecimal("5.0")) > 0) {
            throw new BadRequestException("Рейтинг должен быть от 0.0 до 5.0");
        }
        Volunteer v = volunteerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + id));
        v.setManualRatingSum(v.getManualRatingSum().add(newRating));
        v.setManualRatingCount(v.getManualRatingCount() + 1);
        volunteerRepository.save(v);
        syncVolunteerAggregates(id);
        return findById(id);
    }

    /** Пересчёт среднего рейтинга и числа успешно завершённых задач по заявкам. */
    @Transactional
    public void syncVolunteerAggregates(Long volunteerId) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        BigDecimal appSum = taskApplicationRepository.sumOrganizerRatings(volunteerId);
        long appCnt = taskApplicationRepository.countOrganizerRatings(volunteerId);
        BigDecimal manSum = v.getManualRatingSum();
        long manCnt = v.getManualRatingCount();
        BigDecimal total = appSum.add(manSum);
        long cnt = appCnt + manCnt;
        v.setRating(cnt == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(cnt), 2, RoundingMode.HALF_UP));
        long successes = taskApplicationRepository.countByVolunteerAndStatus(v, ApplicationStatus.COMPLETED_SUCCESS);
        v.setCompletedTasksCount((int) successes);
        volunteerRepository.save(v);
    }

    /** UC16: Статистика волонтёра */
    @Transactional(readOnly = true)
    public VolunteerStatsDto getStats(Long volunteerId) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        List<TaskApplication> apps = taskApplicationRepository.findByVolunteer(v);
        long pending = apps.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).count();
        long approved = apps.stream().filter(a ->
                a.getStatus() == ApplicationStatus.APPROVED
                        || a.getStatus() == ApplicationStatus.COMPLETED_SUCCESS
                        || a.getStatus() == ApplicationStatus.COMPLETED_FAILURE).count();
        return new VolunteerStatsDto(v.getId(), v.getFullName(), v.getRating(),
                v.getCompletedTasksCount(), (int) pending, (int) approved);
    }

    /** UC17: устарело — используйте закрытие заявки с оценкой (PATCH /api/applications/{id}/close). */
    @Transactional
    public VolunteerDto markTaskCompleted(Long volunteerId, Long applicationId) {
        TaskApplication app = taskApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + applicationId));
        if (!app.getVolunteer().getId().equals(volunteerId)) {
            throw new BadRequestException("Заявка не принадлежит этому волонтёру");
        }
        throw new BadRequestException(
                "Завершение с учётом рейтинга: PATCH /api/applications/{id}/close с телом { \"successful\": true/false, \"rating\": 0..5 }");
    }

    /** UC18: Волонтёр подаёт заявку на задачу (лимит 2 попытки) */
    @Transactional
    public TaskApplicationDto applyToTask(Long volunteerId, Long taskId, String message) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        if (!v.isActive()) {
            throw new BadRequestException("Неактивный волонтёр не может подавать заявки");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + taskId));
        if (task.getStatus() != TaskStatus.OPEN) {
            throw new BadRequestException("Подавать заявки можно только на открытые задачи");
        }

        List<TaskApplication> prevApps = taskApplicationRepository.findByVolunteerAndTaskId(v, taskId);

        // Проверяем есть ли активная заявка (PENDING или APPROVED)
        boolean hasActive = prevApps.stream()
                .anyMatch(a -> a.getStatus() == ApplicationStatus.PENDING
                        || a.getStatus() == ApplicationStatus.APPROVED);
        if (hasActive) {
            throw new BadRequestException("У вас уже есть активная заявка на эту задачу");
        }

        // Лимит: не более 2 попыток
        int totalAttempts = prevApps.size();
        if (totalAttempts >= 2) {
            throw new BadRequestException("Лимит исчерпан: вы уже подавали заявку на эту задачу 2 раза");
        }

        TaskApplication app = TaskApplication.builder()
                .task(task)
                .volunteer(v)
                .status(ApplicationStatus.PENDING)
                .message(message)
                .attemptNumber(totalAttempts + 1)
                .build();
        return toApplicationDto(taskApplicationRepository.save(app));
    }

    /** UC19: Просмотр доступных (открытых) задач */
    @Transactional(readOnly = true)
    public List<TaskDto> getAvailableTasks() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.OPEN)
                .map(this::toTaskDto).toList();
    }

    /** Статус заявки волонтёра на конкретную задачу */
    @Transactional(readOnly = true)
    public java.util.Optional<TaskApplicationDto> getApplicationForTask(Long volunteerId, Long taskId) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        return taskApplicationRepository.findByVolunteerAndTaskId(v, taskId).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING
                        || a.getStatus() == ApplicationStatus.APPROVED)
                .findFirst()
                .map(this::toApplicationDto);
    }

    /** UC20: История отклонённых заявок волонтёра */
    @Transactional(readOnly = true)
    public List<TaskApplicationDto> getRejectedApplications(Long volunteerId) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        return taskApplicationRepository.findByVolunteerAndStatus(v, ApplicationStatus.REJECTED)
                .stream().map(this::toApplicationDto).toList();
    }

    /** UC21: Обновить только аватарку */
    @Transactional
    public VolunteerDto updateAvatar(Long id, String avatarUrl) {
        Volunteer v = volunteerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + id));
        v.setAvatarUrl(avatarUrl);
        return toDto(volunteerRepository.save(v));
    }

    private VolunteerDto toDto(Volunteer v) {
        return new VolunteerDto(
                v.getId(), v.getUser().getId(), v.getFullName(), v.getPhone(),
                v.getRegion(), v.getBio(), v.isActive(), v.getBirthDate(),
                v.getRating(), v.getCompletedTasksCount(), v.getAvatarUrl());
    }

    private TaskApplicationDto toApplicationDto(TaskApplication a) {
        return new TaskApplicationDto(
                a.getId(),
                a.getTask().getId(),
                a.getVolunteer().getId(),
                a.getStatus(),
                a.getMessage(),
                a.getAppliedAt(),
                a.getOrganizerRating(),
                a.getTaskCompletedSuccessfully(),
                a.getOrganizerReviewedAt());
    }

    private TaskDto toTaskDto(Task t) {
        return new TaskDto(t.getId(), t.getTitle(), t.getDescription(),
                t.getOrganizer().getId(), t.getCategory() != null ? t.getCategory().getId() : null,
                t.getStatus(), t.getLocation(), t.getStartTime(), t.getEndTime(), t.getCreatedAt());
    }
}
