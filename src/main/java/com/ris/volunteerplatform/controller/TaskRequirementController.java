package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.TaskRequirementDto;
import com.ris.volunteerplatform.dto.TaskRequirementRequest;
import com.ris.volunteerplatform.service.TaskRequirementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-requirements")
@RequiredArgsConstructor
public class TaskRequirementController {

    private final TaskRequirementService taskRequirementService;

    @GetMapping
    public List<TaskRequirementDto> list() {
        return taskRequirementService.findAll();
    }

    /** Требования к навыкам для одной задачи (карточка задачи для волонтёра). */
    @GetMapping("/by-task/{taskId}")
    public List<TaskRequirementDto> byTask(@PathVariable Long taskId) {
        return taskRequirementService.findByTaskId(taskId);
    }

    @GetMapping("/{id}")
    public TaskRequirementDto get(@PathVariable Long id) {
        return taskRequirementService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR')")
    public TaskRequirementDto create(@Valid @RequestBody TaskRequirementRequest request) {
        return taskRequirementService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR')")
    public TaskRequirementDto update(@PathVariable Long id, @Valid @RequestBody TaskRequirementRequest request) {
        return taskRequirementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR')")
    public void delete(@PathVariable Long id) {
        taskRequirementService.delete(id);
    }
}
