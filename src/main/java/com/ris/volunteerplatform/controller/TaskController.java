package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.RecommendedVolunteerDto;
import com.ris.volunteerplatform.dto.TaskDto;
import com.ris.volunteerplatform.dto.TaskRequest;
import com.ris.volunteerplatform.service.MatchingService;
import com.ris.volunteerplatform.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final MatchingService matchingService;

    @GetMapping
    public List<TaskDto> list() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public TaskDto get(@PathVariable Long id) {
        return taskService.findById(id);
    }

    /** Только ORGANIZER создаёт задачи */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public TaskDto create(@Valid @RequestBody TaskRequest request, Authentication authentication) {
        return taskService.create(request, authentication);
    }

    /** ORGANIZER редактирует свои, COORDINATOR/ADMIN — любые */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR','ADMIN')")
    public TaskDto update(@PathVariable Long id, @Valid @RequestBody TaskRequest request,
                          Authentication authentication) {
        return taskService.update(id, request, authentication);
    }

    /** ORGANIZER удаляет свои, COORDINATOR/ADMIN — любые */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR','ADMIN')")
    public void delete(@PathVariable Long id, Authentication authentication) {
        taskService.delete(id, authentication);
    }

    @GetMapping("/{taskId}/recommended-volunteers")
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR','ADMIN')")
    public List<RecommendedVolunteerDto> recommendedVolunteers(@PathVariable Long taskId,
                                                               Authentication authentication) {
        matchingService.authorizeTaskRanking(taskId, authentication);
        return matchingService.computeAndPersistRankings(taskId);
    }
}
