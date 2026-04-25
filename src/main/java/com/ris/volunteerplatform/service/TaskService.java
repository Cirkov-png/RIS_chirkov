package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.TaskDto;
import com.ris.volunteerplatform.dto.TaskRequest;
import com.ris.volunteerplatform.entity.ApplicationStatus;
import com.ris.volunteerplatform.entity.Task;
import com.ris.volunteerplatform.entity.User;
import com.ris.volunteerplatform.entity.UserRole;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.CategoryRepository;
import com.ris.volunteerplatform.repository.MatchResultRepository;
import com.ris.volunteerplatform.repository.TaskApplicationRepository;
import com.ris.volunteerplatform.repository.TaskRepository;
import com.ris.volunteerplatform.repository.TaskRequirementRepository;
import com.ris.volunteerplatform.repository.UserRepository;
import com.ris.volunteerplatform.security.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TaskApplicationRepository taskApplicationRepository;
    private final TaskRequirementRepository taskRequirementRepository;
    private final MatchResultRepository matchResultRepository;

    @Transactional(readOnly = true)
    public List<TaskDto> findAll() {
        return taskRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TaskDto findById(Long id) {
        return taskRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + id));
    }

    /**
     * Создание задачи: организатор — текущий пользователь; координатор может указать {@code organizerUserId}.
     */
    @Transactional
    public TaskDto create(TaskRequest req, Authentication authentication) {
        User current = resolveUser(authentication);
        if (current.getRole() != UserRole.ORGANIZER && current.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Создавать задачи могут только организаторы");
        }
        User organizer = current;
        if (current.getRole() == UserRole.ADMIN && req.organizerUserId() != null) {
            organizer = userRepository.findById(req.organizerUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Организатор не найден: " + req.organizerUserId()));
        }
        Task.TaskBuilder b = Task.builder()
                .title(req.title())
                .description(req.description())
                .organizer(organizer)
                .status(req.status())
                .location(req.location())
                .startTime(req.startTime())
                .endTime(req.endTime());
        if (req.categoryId() != null) {
            b.category(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена: " + req.categoryId())));
        }
        return toDto(taskRepository.save(b.build()));
    }

    @Transactional
    public TaskDto update(Long id, TaskRequest req, Authentication authentication) {
        Task t = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + id));
        assertCanModifyTask(t, authentication);
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setStatus(req.status());
        t.setLocation(req.location());
        t.setStartTime(req.startTime());
        t.setEndTime(req.endTime());
        if (req.categoryId() == null) {
            t.setCategory(null);
        } else {
            t.setCategory(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена: " + req.categoryId())));
        }
        return toDto(taskRepository.save(t));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Task t = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + id));
        assertCanModifyTask(t, authentication);
        var blocking = EnumSet.of(
                ApplicationStatus.APPROVED,
                ApplicationStatus.COMPLETED_SUCCESS,
                ApplicationStatus.COMPLETED_FAILURE);
        if (taskApplicationRepository.existsByTask_IdAndStatusIn(id, blocking)) {
            throw new BadRequestException(
                    "Нельзя удалить задачу: есть одобренный волонтёр или заявка уже закрыта с оценкой. "
                            + "Удаление доступно только до одобрения отклика.");
        }
        matchResultRepository.deleteByTaskId(id);
        taskRequirementRepository.deleteByTaskId(id);
        taskApplicationRepository.deleteByTask_Id(id);
        taskRepository.delete(t);
    }

    private void assertCanModifyTask(Task t, Authentication authentication) {
        User current = resolveUser(authentication);
        // COORDINATOR и ADMIN могут изменять любые задачи
        if (current.getRole() == UserRole.COORDINATOR || current.getRole() == UserRole.ADMIN) {
            return;
        }
        // ORGANIZER только свои
        if (current.getRole() == UserRole.ORGANIZER
                && t.getOrganizer().getId().equals(current.getId())) {
            return;
        }
        throw new AccessDeniedException("Нет прав изменять эту задачу");
    }

    private User resolveUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUserDetails sud)) {
            throw new AccessDeniedException("Требуется аутентификация");
        }
        return sud.getUser();
    }

    private TaskDto toDto(Task t) {
        return new TaskDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getOrganizer().getId(),
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getStatus(),
                t.getLocation(),
                t.getStartTime(),
                t.getEndTime(),
                t.getCreatedAt());
    }
}
