package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.TaskApplicationDto;
import com.ris.volunteerplatform.entity.*;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.repository.TaskApplicationRepository;
import com.ris.volunteerplatform.repository.TaskRepository;
import com.ris.volunteerplatform.repository.UserRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import com.ris.volunteerplatform.security.SecurityUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskApplicationServiceTest {

    @Mock
    private TaskApplicationRepository taskApplicationRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private VolunteerRepository volunteerRepository;
    @Mock
    private UserRepository userRepository;

    private VolunteerService volunteerService;
    private TaskApplicationService taskApplicationService;

    @BeforeEach
    void setUp() {
        volunteerService = spy(new VolunteerService(
                volunteerRepository, userRepository, taskApplicationRepository, taskRepository));
        lenient().doNothing().when(volunteerService).syncVolunteerAggregates(anyLong());
        taskApplicationService = new TaskApplicationService(
                taskApplicationRepository, taskRepository, volunteerRepository, volunteerService);
    }

    @Test
    @DisplayName("Одобрение возможно только из PENDING")
    void approve_onlyFromPending() {
        TaskApplication app = pendingApplication();
        when(taskApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(taskApplicationRepository.save(any())).thenAnswer(a -> a.getArgument(0));

        TaskApplicationDto dto = taskApplicationService.approve(1L);

        assertThat(dto.status()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("Отклонение из APPROVED → ошибка")
    void reject_whenNotPending_fails() {
        TaskApplication app = pendingApplication();
        app.setStatus(ApplicationStatus.APPROVED);
        when(taskApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> taskApplicationService.reject(1L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Закрытие с оценкой: организатор задачи может выполнить")
    void closeWithReview_organizerOk() {
        User organizer = User.builder().id(5L).username("org").role(UserRole.ORGANIZER).build();
        Task task = Task.builder().id(10L).title("x").organizer(organizer).status(TaskStatus.OPEN).build();
        Volunteer volunteer = Volunteer.builder().id(3L).user(User.builder().id(7L).build()).build();
        TaskApplication app = TaskApplication.builder()
                .id(1L)
                .task(task)
                .volunteer(volunteer)
                .status(ApplicationStatus.APPROVED)
                .build();

        when(taskApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        SecurityUserDetails sud = new SecurityUserDetails(organizer);
        Authentication auth = new TestingAuthenticationToken(sud, null, sud.getAuthorities());

        TaskApplicationDto dto = taskApplicationService.closeWithOrganizerReview(
                1L, true, new BigDecimal("4.5"), auth);

        assertThat(dto.status()).isEqualTo(ApplicationStatus.COMPLETED_SUCCESS);
        verify(taskRepository).save(argThat((Task t) -> t.getStatus() == TaskStatus.COMPLETED));
        verify(volunteerService).syncVolunteerAggregates(3L);
    }

    @Test
    @DisplayName("Закрытие с оценкой: чужой организатор получает AccessDenied")
    void closeWithReview_wrongOrganizer() {
        User organizer = User.builder().id(5L).role(UserRole.ORGANIZER).build();
        User other = User.builder().id(99L).role(UserRole.ORGANIZER).build();
        Task task = Task.builder().id(10L).title("x").organizer(organizer).status(TaskStatus.OPEN).build();
        Volunteer volunteer = Volunteer.builder().id(3L).user(User.builder().id(7L).build()).build();
        TaskApplication app = TaskApplication.builder().id(1L).task(task).volunteer(volunteer)
                .status(ApplicationStatus.APPROVED).build();

        when(taskApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        SecurityUserDetails sud = new SecurityUserDetails(other);
        Authentication auth = new TestingAuthenticationToken(sud, null, sud.getAuthorities());

        assertThatThrownBy(() ->
                taskApplicationService.closeWithOrganizerReview(1L, true, new BigDecimal("3"), auth))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Оценка вне диапазона → BadRequest")
    void closeWithReview_invalidRating() {
        assertThatThrownBy(() ->
                taskApplicationService.closeWithOrganizerReview(1L, true, new BigDecimal("10"), null))
                .isInstanceOf(BadRequestException.class);
    }

    private static TaskApplication pendingApplication() {
        User organizer = User.builder().id(5L).build();
        Task task = Task.builder().id(10L).organizer(organizer).title("t").status(TaskStatus.OPEN).build();
        Volunteer volunteer = Volunteer.builder().id(3L).user(User.builder().id(8L).build()).build();
        return TaskApplication.builder()
                .id(1L)
                .task(task)
                .volunteer(volunteer)
                .status(ApplicationStatus.PENDING)
                .build();
    }
}
