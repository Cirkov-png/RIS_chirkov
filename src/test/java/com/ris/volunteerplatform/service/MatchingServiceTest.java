package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.RecommendedVolunteerDto;
import com.ris.volunteerplatform.entity.*;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.*;
import com.ris.volunteerplatform.security.SecurityUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskRequirementRepository taskRequirementRepository;
    @Mock
    private VolunteerRepository volunteerRepository;
    @Mock
    private VolunteerSkillRepository volunteerSkillRepository;
    @Mock
    private MatchResultRepository matchResultRepository;
    @Mock
    private TaskApplicationRepository taskApplicationRepository;

    @InjectMocks
    private MatchingService matchingService;

    @Test
    @DisplayName("Нет активных откликов → пустой рейтинг")
    void ranking_noApplicants_empty() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(Task.builder().id(1L).build()));
        when(taskApplicationRepository.findByTaskId(1L)).thenReturn(List.of());

        List<RecommendedVolunteerDto> res = matchingService.computeAndPersistRankings(1L);

        assertThat(res).isEmpty();
        verify(matchResultRepository).deleteByTaskId(1L);
        verify(matchResultRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Рейтинг соответствует навыкам и весам")
    void ranking_withRequirements_computesScore() {
        Task task = Task.builder().id(50L).title("job").organizer(User.builder().id(1L).build())
                .status(TaskStatus.OPEN).build();

        Skill skill = Skill.builder().id(100L).name("Перевод").build();

        TaskRequirement tr = TaskRequirement.builder()
                .id(200L).task(task).skill(skill).importanceWeight(new BigDecimal("5")).build();

        User vu = User.builder().id(2L).username("v").build();
        Volunteer v = Volunteer.builder().id(10L).user(vu).fullName("Анна").build();

        TaskApplication appl = TaskApplication.builder()
                .task(task).volunteer(v).status(ApplicationStatus.PENDING).build();

        VolunteerSkill vs = VolunteerSkill.builder()
                .volunteer(v).skill(skill).proficiencyLevel(5).build();

        when(taskRepository.findById(50L)).thenReturn(Optional.of(task));
        when(taskApplicationRepository.findByTaskId(50L)).thenReturn(List.of(appl));
        when(volunteerRepository.findAllById(Set.of(10L))).thenReturn(List.of(v));
        when(taskRequirementRepository.findByTaskId(50L)).thenReturn(List.of(tr));
        when(volunteerSkillRepository.findByVolunteerId(10L)).thenReturn(List.of(vs));
        when(matchResultRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<RecommendedVolunteerDto> res = matchingService.computeAndPersistRankings(50L);

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().matchScore()).isEqualByComparingTo(new BigDecimal("1.000000"));
        verify(matchResultRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Организатор чужой задачи не может запускать матчинг")
    void authorize_notOwner() {
        User owner = User.builder().id(1L).role(UserRole.ORGANIZER).build();
        User other = User.builder().id(2L).role(UserRole.ORGANIZER).build();
        Task task = Task.builder().id(9L).organizer(owner).title("t").status(TaskStatus.OPEN).build();
        when(taskRepository.findById(9L)).thenReturn(Optional.of(task));

        SecurityUserDetails sud = new SecurityUserDetails(other);
        Authentication auth = new TestingAuthenticationToken(sud, null, sud.getAuthorities());

        assertThatThrownBy(() -> matchingService.authorizeTaskRanking(9L, auth))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Координатор может запускать матчинг")
    void authorize_coordinator() {
        User owner = User.builder().id(1L).role(UserRole.ORGANIZER).build();
        User coord = User.builder().id(3L).role(UserRole.COORDINATOR).build();
        Task task = Task.builder().id(9L).organizer(owner).title("t").status(TaskStatus.OPEN).build();
        when(taskRepository.findById(9L)).thenReturn(Optional.of(task));
        SecurityUserDetails sud = new SecurityUserDetails(coord);
        Authentication auth = new TestingAuthenticationToken(sud, null, sud.getAuthorities());

        matchingService.authorizeTaskRanking(9L, auth);

        verify(taskRepository).findById(9L);
    }

    @Test
    @DisplayName("Задача не найдена")
    void authorize_taskMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.authorizeTaskRanking(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
