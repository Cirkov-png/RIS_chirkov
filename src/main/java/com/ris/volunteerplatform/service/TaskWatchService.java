package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.TaskWatchDto;
import com.ris.volunteerplatform.entity.Task;
import com.ris.volunteerplatform.entity.TaskWatch;
import com.ris.volunteerplatform.entity.Volunteer;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.TaskRepository;
import com.ris.volunteerplatform.repository.TaskWatchRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskWatchService {

    private final TaskWatchRepository taskWatchRepository;
    private final VolunteerRepository volunteerRepository;
    private final TaskRepository taskRepository;

    /** UC: Волонтёр добавляет задачу в отслеживание */
    @Transactional
    public TaskWatchDto watch(Long volunteerId, UUID taskUuid) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        Task task = taskRepository.findByUuid(taskUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + taskUuid));

        if (taskWatchRepository.existsByVolunteerAndTask(v, task)) {
            throw new BadRequestException("Задача уже в списке отслеживания");
        }

        TaskWatch watch = TaskWatch.builder()
                .volunteer(v)
                .task(task)
                .build();
        return toDto(taskWatchRepository.save(watch));
    }

    /** UC: Волонтёр убирает задачу из отслеживания */
    @Transactional
    public void unwatch(Long volunteerId, UUID taskUuid) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        Task task = taskRepository.findByUuid(taskUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + taskUuid));
        TaskWatch watch = taskWatchRepository.findByVolunteerAndTask(v, task)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена в отслеживании"));
        taskWatchRepository.delete(watch);
    }

    /** UC: Волонтёр просматривает список отслеживаемых задач */
    @Transactional(readOnly = true)
    public List<TaskWatchDto> getWatchlist(Long volunteerId) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        return taskWatchRepository.findByVolunteer(v).stream()
                .map(this::toDto).toList();
    }

    /** UC: Просмотр задач с истекающим дедлайном (до N дней) */
    @Transactional(readOnly = true)
    public List<TaskWatchDto> getExpiringWatches(Long volunteerId, int withinDays) {
        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + volunteerId));
        Instant threshold = Instant.now().plus(withinDays, ChronoUnit.DAYS);
        return taskWatchRepository.findByVolunteer(v).stream()
                .filter(tw -> tw.getTask().getEndTime() != null
                        && tw.getTask().getEndTime().isBefore(threshold)
                        && tw.getTask().getEndTime().isAfter(Instant.now()))
                .map(this::toDto).toList();
    }

    /**
     * Scheduled job: каждый час проверяем дедлайны.
     * Помечает записи как notifiedDeadline=true когда до дедлайна <= reminderDays.
     */
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void checkDeadlines() {
        List<TaskWatch> pending = taskWatchRepository.findPendingDeadlineNotifications();
        Instant now = Instant.now();
        int notified = 0;
        for (TaskWatch tw : pending) {
            Task task = tw.getTask();
            if (task.getEndTime() == null) continue;
            long daysLeft = ChronoUnit.DAYS.between(now, task.getEndTime());
            if (daysLeft <= task.getReminderDays()) {
                tw.setNotifiedDeadline(true);
                taskWatchRepository.save(tw);
                log.info("Deadline reminder: volunteer={} task='{}' daysLeft={}",
                        tw.getVolunteer().getId(), task.getTitle(), daysLeft);
                notified++;
            }
        }
        if (notified > 0) {
            log.info("checkDeadlines: уведомлено {} записей", notified);
        }
    }

    private TaskWatchDto toDto(TaskWatch tw) {
        Task task = tw.getTask();
        long daysLeft = task.getEndTime() != null
                ? Math.max(0, ChronoUnit.DAYS.between(Instant.now(), task.getEndTime()))
                : -1;
        return new TaskWatchDto(
                tw.getUuid(),
                tw.getVolunteer().getUuid(),
                task.getUuid(),
                task.getTitle(),
                task.getStatus().name(),
                task.getEndTime(),
                tw.getWatchedAt(),
                tw.isNotifiedDeadline(),
                daysLeft);
    }
}
