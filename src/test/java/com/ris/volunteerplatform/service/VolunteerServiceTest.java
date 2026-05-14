package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.TaskApplicationDto;
import com.ris.volunteerplatform.dto.VolunteerDto;
import com.ris.volunteerplatform.dto.VolunteerRequest;
import com.ris.volunteerplatform.dto.VolunteerStatsDto;
import com.ris.volunteerplatform.entity.*;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.TaskApplicationRepository;
import com.ris.volunteerplatform.repository.TaskRepository;
import com.ris.volunteerplatform.repository.UserRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolunteerServiceTest {

    @Mock private VolunteerRepository volunteerRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskApplicationRepository taskApplicationRepository;
    @Mock private TaskRepository taskRepository;

    @InjectMocks
    private VolunteerService volunteerService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private User volunteerUser(long id) {
        return User.builder().id(id).username("u" + id).email("u" + id + "@x.ru")
                .role(UserRole.VOLUNTEER).enabled(true).build();
    }

    private Volunteer volunteer(long id) {
        return Volunteer.builder().id(id).user(volunteerUser(id + 100))
                .fullName("Имя " + id).active(true)
                .rating(BigDecimal.ZERO).completedTasksCount(0).build();
    }

    private Task openTask(long id) {
        User org = User.builder().id(99L).role(UserRole.ORGANIZER).build();
        return Task.builder().id(id).title("Задача " + id)
                .organizer(org).status(TaskStatus.OPEN).build();
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: пользователь не VOLUNTEER → BadRequest")
    void create_notVolunteerRole() {
        User org = User.builder().id(5L).role(UserRole.ORGANIZER).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(org));

        assertThatThrownBy(() -> volunteerService.create(
                new VolunteerRequest(5L, "имя", null, null, null, true, null, null)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("create: профиль уже существует → BadRequest")
    void create_alreadyExists() {
        User u = volunteerUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(volunteerRepository.findByUser_Id(1L)).thenReturn(Optional.of(volunteer(1L)));

        assertThatThrownBy(() -> volunteerService.create(
                new VolunteerRequest(1L, "имя", null, null, null, true, null, null)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("create: успешное создание профиля")
    void create_ok() {
        User u = volunteerUser(1L);
        Volunteer saved = volunteer(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(volunteerRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(volunteerRepository.save(any())).thenReturn(saved);

        VolunteerDto dto = volunteerService.create(
                new VolunteerRequest(1L, "Имя 1", null, null, null, true, null, null));

        assertThat(dto.id()).isEqualTo(1L);
        verify(volunteerRepository).save(any());
    }

    // ── deactivate / activate ────────────────────────────────────────────────

    @Test
    @DisplayName("deactivate выключает флаг active")
    void deactivate() {
        Volunteer v = volunteer(1L);
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(volunteerRepository.save(v)).thenReturn(v);

        volunteerService.deactivate(1L);

        assertThat(v.isActive()).isFalse();
        verify(volunteerRepository).save(v);
    }

    @Test
    @DisplayName("activate включает флаг active")
    void activate() {
        Volunteer v = volunteer(1L);
        v.setActive(false);
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(volunteerRepository.save(v)).thenReturn(v);

        volunteerService.activate(1L);

        assertThat(v.isActive()).isTrue();
    }

    // ── findByUserId ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUserId: не найден → ResourceNotFoundException")
    void findByUserId_notFound() {
        when(volunteerRepository.findByUser_Id(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerService.findByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findByUserId: найден → возвращает DTO")
    void findByUserId_ok() {
        Volunteer v = volunteer(1L);
        when(volunteerRepository.findByUser_Id(101L)).thenReturn(Optional.of(v));

        VolunteerDto dto = volunteerService.findByUserId(101L);

        assertThat(dto.id()).isEqualTo(1L);
    }

    // ── applyToTask ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("applyToTask: неактивный волонтёр → BadRequest")
    void apply_inactiveVolunteer() {
        Volunteer v = volunteer(1L);
        v.setActive(false);
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> volunteerService.applyToTask(1L, 10L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Неактивный");
    }

    @Test
    @DisplayName("applyToTask: задача не OPEN → BadRequest")
    void apply_taskNotOpen() {
        Volunteer v = volunteer(1L);
        Task task = openTask(10L);
        task.setStatus(TaskStatus.COMPLETED);
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> volunteerService.applyToTask(1L, 10L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("открытые");
    }

    @Test
    @DisplayName("applyToTask: уже есть активная заявка → BadRequest")
    void apply_alreadyActive() {
        Volunteer v = volunteer(1L);
        Task task = openTask(10L);
        TaskApplication existing = TaskApplication.builder()
                .id(5L).task(task).volunteer(v).status(ApplicationStatus.PENDING).build();

        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskApplicationRepository.findByVolunteerAndTaskId(v, 10L))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> volunteerService.applyToTask(1L, 10L, "msg"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("активная");
    }

    @Test
    @DisplayName("applyToTask: лимит 2 попытки исчерпан → BadRequest")
    void apply_limitExceeded() {
        Volunteer v = volunteer(1L);
        Task task = openTask(10L);
        TaskApplication w1 = TaskApplication.builder().id(1L).task(task).volunteer(v)
                .status(ApplicationStatus.WITHDRAWN).build();
        TaskApplication w2 = TaskApplication.builder().id(2L).task(task).volunteer(v)
                .status(ApplicationStatus.WITHDRAWN).build();

        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskApplicationRepository.findByVolunteerAndTaskId(v, 10L))
                .thenReturn(List.of(w1, w2));

        assertThatThrownBy(() -> volunteerService.applyToTask(1L, 10L, "msg"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Лимит");
    }

    @Test
    @DisplayName("applyToTask: первая заявка создаётся успешно")
    void apply_firstTime_ok() {
        Volunteer v = volunteer(1L);
        Task task = openTask(10L);
        TaskApplication saved = TaskApplication.builder().id(99L).task(task).volunteer(v)
                .status(ApplicationStatus.PENDING).attemptNumber(1).build();

        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskApplicationRepository.findByVolunteerAndTaskId(v, 10L)).thenReturn(List.of());
        when(taskApplicationRepository.save(any())).thenReturn(saved);

        TaskApplicationDto dto = volunteerService.applyToTask(1L, 10L, "хочу помочь");

        assertThat(dto.status()).isEqualTo(ApplicationStatus.PENDING);
        verify(taskApplicationRepository).save(any());
    }

    @Test
    @DisplayName("applyToTask: повторная заявка после отзыва (2-я попытка) — успешно")
    void apply_secondAttempt_ok() {
        Volunteer v = volunteer(1L);
        Task task = openTask(10L);
        TaskApplication withdrawn = TaskApplication.builder().id(1L).task(task).volunteer(v)
                .status(ApplicationStatus.WITHDRAWN).build();
        TaskApplication saved = TaskApplication.builder().id(100L).task(task).volunteer(v)
                .status(ApplicationStatus.PENDING).attemptNumber(2).build();

        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskApplicationRepository.findByVolunteerAndTaskId(v, 10L))
                .thenReturn(List.of(withdrawn));
        when(taskApplicationRepository.save(any())).thenReturn(saved);

        TaskApplicationDto dto = volunteerService.applyToTask(1L, 10L, null);

        assertThat(dto.status()).isEqualTo(ApplicationStatus.PENDING);
    }

    // ── withdrawApplication ──────────────────────────────────────────────────

    @Test
    @DisplayName("withdraw: не своя заявка → BadRequest")
    void withdraw_notOwn() {
        Volunteer v = volunteer(1L);
        Volunteer other = volunteer(2L);
        Task task = openTask(10L);
        TaskApplication app = TaskApplication.builder().id(5L).task(task).volunteer(other)
                .status(ApplicationStatus.PENDING).build();

        when(taskApplicationRepository.findById(5L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> volunteerService.withdrawApplication(1L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("не ваша");
    }

    @Test
    @DisplayName("withdraw: статус не PENDING → BadRequest")
    void withdraw_notPending() {
        Volunteer v = volunteer(1L);
        Task task = openTask(10L);
        TaskApplication app = TaskApplication.builder().id(5L).task(task).volunteer(v)
                .status(ApplicationStatus.APPROVED).build();

        when(taskApplicationRepository.findById(5L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> volunteerService.withdrawApplication(1L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("withdraw: успешный отзыв меняет статус на WITHDRAWN")
    void withdraw_ok() {
        Volunteer v = volunteer(1L);
        Task task = openTask(10L);
        TaskApplication app = TaskApplication.builder().id(5L).task(task).volunteer(v)
                .status(ApplicationStatus.PENDING).build();

        when(taskApplicationRepository.findById(5L)).thenReturn(Optional.of(app));
        when(taskApplicationRepository.save(app)).thenReturn(app);

        TaskApplicationDto dto = volunteerService.withdrawApplication(1L, 5L);

        assertThat(dto.status()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    // ── rateVolunteer ────────────────────────────────────────────────────────

    @Test
    @DisplayName("rateVolunteer: оценка вне диапазона → BadRequest")
    void rate_outOfRange() {
        assertThatThrownBy(() -> volunteerService.rateVolunteer(1L, new BigDecimal("6.0")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("rateVolunteer: мануальная оценка сохраняется")
    void rate_saves() {
        Volunteer v = volunteer(1L);
        v.setManualRatingSum(BigDecimal.ZERO);
        v.setManualRatingCount(0);
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(volunteerRepository.save(any())).thenReturn(v);
        when(taskApplicationRepository.sumOrganizerRatings(1L)).thenReturn(BigDecimal.ZERO);
        when(taskApplicationRepository.countOrganizerRatings(1L)).thenReturn(0L);
        when(taskApplicationRepository.countByVolunteerAndStatus(v, ApplicationStatus.COMPLETED_SUCCESS)).thenReturn(0L);

        volunteerService.rateVolunteer(1L, new BigDecimal("4.0"));

        verify(volunteerRepository, atLeast(2)).save(any());
    }

    // ── getStats ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getStats: считает pending и approved корректно")
    void stats_counts() {
        Volunteer v = volunteer(1L);
        v.setRating(new BigDecimal("4.50"));
        v.setCompletedTasksCount(3);
        Task task = openTask(10L);

        TaskApplication pending = TaskApplication.builder().id(1L).task(task).volunteer(v)
                .status(ApplicationStatus.PENDING).build();
        TaskApplication approved = TaskApplication.builder().id(2L).task(task).volunteer(v)
                .status(ApplicationStatus.APPROVED).build();
        TaskApplication withdrawn = TaskApplication.builder().id(3L).task(task).volunteer(v)
                .status(ApplicationStatus.WITHDRAWN).build();

        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(v));
        when(taskApplicationRepository.findByVolunteer(v))
                .thenReturn(List.of(pending, approved, withdrawn));

        VolunteerStatsDto stats = volunteerService.getStats(1L);

        assertThat(stats.completedTasksCount()).isEqualTo(3);
        assertThat(stats.pendingApplicationsCount()).isEqualTo(1);
        assertThat(stats.approvedApplicationsCount()).isEqualTo(1);
        assertThat(stats.rating()).isEqualByComparingTo(new BigDecimal("4.50"));
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: не найден → ResourceNotFoundException")
    void delete_notFound() {
        when(volunteerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> volunteerService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete: найден → удаляется")
    void delete_ok() {
        when(volunteerRepository.existsById(1L)).thenReturn(true);

        volunteerService.delete(1L);

        verify(volunteerRepository).deleteById(1L);
    }
}
