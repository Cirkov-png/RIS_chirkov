package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.CloseApplicationRequest;
import com.ris.volunteerplatform.dto.TaskApplicationDto;
import com.ris.volunteerplatform.dto.TaskApplicationRequest;
import com.ris.volunteerplatform.service.TaskApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class TaskApplicationController {

    private final TaskApplicationService taskApplicationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER','ADMIN')")
    public List<TaskApplicationDto> list() {
        return taskApplicationService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER','VOLUNTEER','ADMIN')")
    public TaskApplicationDto get(@PathVariable Long id) {
        return taskApplicationService.findById(id);
    }

    /** Заявки по задаче — для организатора */
    @GetMapping("/by-task/{taskId}")
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER','ADMIN')")
    public List<TaskApplicationDto> byTask(@PathVariable Long taskId) {
        return taskApplicationService.findByTaskId(taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public TaskApplicationDto create(@Valid @RequestBody TaskApplicationRequest request) {
        return taskApplicationService.create(request);
    }

    /** Одобрить заявку */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR','ADMIN')")
    public TaskApplicationDto approve(@PathVariable Long id) {
        return taskApplicationService.approve(id);
    }

    /** Отклонить заявку */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR','ADMIN')")
    public TaskApplicationDto reject(@PathVariable Long id) {
        return taskApplicationService.reject(id);
    }

    /** Закрыть одобренную заявку: выполнено / не выполнено и оценка волонтёра */
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR','ADMIN')")
    public TaskApplicationDto close(@PathVariable Long id,
                                    @Valid @RequestBody CloseApplicationRequest body,
                                    Authentication authentication) {
        return taskApplicationService.closeWithOrganizerReview(id, body.successful(), body.rating(), authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER','ADMIN')")
    public TaskApplicationDto update(@PathVariable Long id, @Valid @RequestBody TaskApplicationRequest request) {
        return taskApplicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('COORDINATOR','ADMIN')")
    public void delete(@PathVariable Long id) {
        taskApplicationService.delete(id);
    }
}
