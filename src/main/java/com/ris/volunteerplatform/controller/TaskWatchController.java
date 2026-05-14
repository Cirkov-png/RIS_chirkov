package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.TaskWatchDto;
import com.ris.volunteerplatform.service.TaskWatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/volunteers/{volunteerId}/watchlist")
@RequiredArgsConstructor
public class TaskWatchController {

    private final TaskWatchService taskWatchService;

    /** UC: Получить список отслеживаемых задач */
    @GetMapping
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ORGANIZER','ADMIN')")
    public List<TaskWatchDto> getWatchlist(@PathVariable Long volunteerId) {
        return taskWatchService.getWatchlist(volunteerId);
    }

    /** UC: Задачи с истекающим дедлайном (по умолчанию 3 дня) */
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public List<TaskWatchDto> getExpiring(
            @PathVariable Long volunteerId,
            @RequestParam(defaultValue = "3") int withinDays) {
        return taskWatchService.getExpiringWatches(volunteerId, withinDays);
    }

    /** UC: Добавить задачу в отслеживание */
    @PostMapping("/{taskUuid}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public TaskWatchDto watch(@PathVariable Long volunteerId,
                               @PathVariable UUID taskUuid) {
        return taskWatchService.watch(volunteerId, taskUuid);
    }

    /** UC: Убрать задачу из отслеживания */
    @DeleteMapping("/{taskUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public void unwatch(@PathVariable Long volunteerId,
                         @PathVariable UUID taskUuid) {
        taskWatchService.unwatch(volunteerId, taskUuid);
    }
}
