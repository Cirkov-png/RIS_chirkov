package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.TaskDto;
import com.ris.volunteerplatform.dto.TaskRequest;
import com.ris.volunteerplatform.entity.*;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.CategoryRepository;
import com.ris.volunteerplatform.repository.MatchResultRepository;
import com.ris.volunteerplatform.repository.TaskApplicationRepository;
import com.ris.volunteerplatform.repository.TaskRepository;
import com.ris.volunteerplatform.repository.TaskRequirementRepository;
import com.ris.volunteerplatform.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TaskApplicationRepository taskApplicationRepository;
    @Mock private TaskRequirementRepository taskRequirementRepository;
    @Mock private MatchResultRepository matchResultRepository;

    @InjectMocks
    private TaskService taskService;

    private Authentication authAs(User user) {
        SecurityUserDetails sud = new SecurityUserDetails(user);
        return new TestingAuthenticationToken(sud, null, sud.getAuthorities());
    }

    private User organizer(long id) {
        return User.builder().id(id).username("org" + id).role(UserRole.ORGANIZER).enabled(true).build();
    }

    private User coordinator(long id) {
        return User.builder().id(id).username("coord" + id).role(UserRole.COORDINATOR).enabled(true).build();
    }

    private TaskRequest openRequest() {
        return new TaskRequest("Задача", "Описание", null, TaskStatus.OPEN, "Минск", null, null, null);
    }

    // ── findAll / findById ───────────────────────────────────────────────────

    @Test
    @DisplayName("findAll возвращает все задачи")
    void findAll() {
        User org = organizer(1L);
        Task t = Task.builder().id(1L).title("T").organizer(org).status(TaskStatus.OPEN).build();
        when(taskRepository.findAll()).thenReturn(List.of(t));

        List<TaskDto> result = taskService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("T");
    }

    @Test
    @DisplayName("findById: не найдена → ResourceNotFoundException")
    void findById_notFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: организатор создаёт задачу на себя")
    void create_organizer_ok() {
        User org = organizer(1L);
        Task saved = Task.builder().id(10L).title("Задача").organizer(org)
                .status(TaskStatus.OPEN).build();
        when(taskRepository.save(any())).thenReturn(saved);

        TaskDto dto = taskService.create(openRequest(), authAs(org));

        assertThat(dto.organizerId()).isEqualTo(1L);
        verify(taskRepository).save(any());
    }

    @Test
    @DisplayName("create: волонтёр не может создавать задачи → AccessDenied")
    void create_volunteer_denied() {
        User vol = User.builder().id(5L).role(UserRole.VOLUNTEER).build();

        assertThatThrownBy(() -> taskService.create(openRequest(), authAs(vol)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("create: координатор не может создавать задачи → AccessDenied")
    void create_coordinator_denied() {
        User coord = coordinator(3L);

        assertThatThrownBy(() -> taskService.create(openRequest(), authAs(coord)))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: организатор редактирует свою задачу")
    void update_ownTask_ok() {
        User org = organizer(1L);
        Task task = Task.builder().id(10L).title("Старое").organizer(org)
                .status(TaskStatus.OPEN).build();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        TaskDto dto = taskService.update(10L, openRequest(), authAs(org));

        assertThat(dto.title()).isEqualTo("Задача");
    }

    @Test
    @DisplayName("update: организатор не может редактировать чужую задачу → AccessDenied")
    void update_otherTask_denied() {
        User owner = organizer(1L);
        User other = organizer(2L);
        Task task = Task.builder().id(10L).title("T").organizer(owner)
                .status(TaskStatus.OPEN).build();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.update(10L, openRequest(), authAs(other)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("update: координатор может редактировать любую задачу")
    void update_coordinator_ok() {
        User owner = organizer(1L);
        User coord = coordinator(3L);
        Task task = Task.builder().id(10L).title("T").organizer(owner)
                .status(TaskStatus.OPEN).build();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        TaskDto dto = taskService.update(10L, openRequest(), authAs(coord));

        assertThat(dto).isNotNull();
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: организатор удаляет свою задачу")
    void delete_ownTask_ok() {
        User org = organizer(1L);
        Task task = Task.builder().id(10L).title("T").organizer(org)
                .status(TaskStatus.OPEN).build();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskApplicationRepository.existsByTask_IdAndStatusIn(eq(10L), any())).thenReturn(false);

        taskService.delete(10L, authAs(org));

        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("delete: организатор не может удалить чужую задачу → AccessDenied")
    void delete_otherTask_denied() {
        User owner = organizer(1L);
        User other = organizer(2L);
        Task task = Task.builder().id(10L).title("T").organizer(owner)
                .status(TaskStatus.OPEN).build();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.delete(10L, authAs(other)))
                .isInstanceOf(AccessDeniedException.class);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete: координатор удаляет любую задачу")
    void delete_coordinator_ok() {
        User owner = organizer(1L);
        User coord = coordinator(3L);
        Task task = Task.builder().id(10L).title("T").organizer(owner)
                .status(TaskStatus.OPEN).build();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskApplicationRepository.existsByTask_IdAndStatusIn(eq(10L), any())).thenReturn(false);

        taskService.delete(10L, authAs(coord));

        verify(taskRepository).delete(task);
    }
}
