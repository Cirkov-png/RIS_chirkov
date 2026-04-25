package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.TaskApplicationDto;
import com.ris.volunteerplatform.dto.TaskApplicationRequest;
import com.ris.volunteerplatform.entity.ApplicationStatus;
import com.ris.volunteerplatform.entity.Task;
import com.ris.volunteerplatform.entity.TaskApplication;
import com.ris.volunteerplatform.entity.TaskStatus;
import com.ris.volunteerplatform.entity.User;
import com.ris.volunteerplatform.entity.UserRole;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.TaskApplicationRepository;
import com.ris.volunteerplatform.repository.TaskRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import com.ris.volunteerplatform.security.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskApplicationService {

    private final TaskApplicationRepository taskApplicationRepository;
    private final TaskRepository taskRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerService volunteerService;

    @Transactional(readOnly = true)
    public List<TaskApplicationDto> findAll() {
        return taskApplicationRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TaskApplicationDto findById(Long id) {
        return taskApplicationRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + id));
    }

    @Transactional(readOnly = true)
    public List<TaskApplicationDto> findByTaskId(Long taskId) {
        return taskApplicationRepository.findByTaskId(taskId).stream().map(this::toDto).toList();
    }

    @Transactional
    public TaskApplicationDto create(TaskApplicationRequest req) {
        var task = taskRepository.findById(req.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + req.taskId()));
        var volunteer = volunteerRepository.findById(req.volunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + req.volunteerId()));
        ApplicationStatus st = req.status() != null ? req.status() : ApplicationStatus.PENDING;
        TaskApplication app = TaskApplication.builder()
                .task(task).volunteer(volunteer).status(st).message(req.message()).build();
        return toDto(taskApplicationRepository.save(app));
    }

    /** Организатор одобряет заявку */
    @Transactional
    public TaskApplicationDto approve(Long id) {
        TaskApplication app = taskApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + id));
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Одобрить можно только заявку в статусе PENDING");
        }
        app.setStatus(ApplicationStatus.APPROVED);
        return toDto(taskApplicationRepository.save(app));
    }

    /** Организатор отклоняет заявку */
    @Transactional
    public TaskApplicationDto reject(Long id) {
        TaskApplication app = taskApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + id));
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Отклонить можно только заявку в статусе PENDING");
        }
        app.setStatus(ApplicationStatus.REJECTED);
        return toDto(taskApplicationRepository.save(app));
    }

    @Transactional
    public TaskApplicationDto update(Long id, TaskApplicationRequest req) {
        TaskApplication app = taskApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + id));
        app.setTask(taskRepository.findById(req.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + req.taskId())));
        app.setVolunteer(volunteerRepository.findById(req.volunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + req.volunteerId())));
        if (req.status() != null) app.setStatus(req.status());
        app.setMessage(req.message());
        return toDto(taskApplicationRepository.save(app));
    }

    @Transactional
    public void delete(Long id) {
        if (!taskApplicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Заявка не найдена: " + id);
        }
        taskApplicationRepository.deleteById(id);
    }

    /**
     * Организатор (или координатор/админ) закрывает одобренную заявку: выполнено/не выполнено и оценка.
     * Средний рейтинг волонтёра пересчитывается по всем оценкам с заявок и ручным оценкам координатора.
     */
    @Transactional
    public TaskApplicationDto closeWithOrganizerReview(Long applicationId, boolean successful, BigDecimal rating,
                                                        Authentication authentication) {
        if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5.0")) > 0) {
            throw new BadRequestException("Оценка должна быть от 0.0 до 5.0");
        }
        TaskApplication app = taskApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + applicationId));
        if (app.getStatus() != ApplicationStatus.APPROVED) {
            throw new BadRequestException("Закрыть с оценкой можно только одобренную заявку");
        }
        Task task = app.getTask();
        assertOrganizerOrElevated(task, authentication);

        app.setOrganizerRating(rating);
        app.setTaskCompletedSuccessfully(successful);
        app.setOrganizerReviewedAt(Instant.now());
        app.setStatus(successful ? ApplicationStatus.COMPLETED_SUCCESS : ApplicationStatus.COMPLETED_FAILURE);
        taskApplicationRepository.save(app);

        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        volunteerService.syncVolunteerAggregates(app.getVolunteer().getId());
        return toDto(app);
    }

    private void assertOrganizerOrElevated(Task task, Authentication authentication) {
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
        throw new AccessDeniedException("Только организатор задачи или координатор могут закрыть заявку");
    }

    private TaskApplicationDto toDto(TaskApplication a) {
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
}
