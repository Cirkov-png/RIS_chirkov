package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.Task;
import com.ris.volunteerplatform.entity.TaskWatch;
import com.ris.volunteerplatform.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskWatchRepository extends JpaRepository<TaskWatch, Long> {

    List<TaskWatch> findByVolunteer(Volunteer volunteer);

    Optional<TaskWatch> findByVolunteerAndTask(Volunteer volunteer, Task task);

    boolean existsByVolunteerAndTask(Volunteer volunteer, Task task);

    Optional<TaskWatch> findByUuid(UUID uuid);

    /** Все отслеживания задач у которых дедлайн скоро и ещё не уведомляли */
    @Query("""
            SELECT tw FROM TaskWatch tw
            JOIN FETCH tw.task t
            JOIN FETCH tw.volunteer v
            WHERE tw.notifiedDeadline = false
              AND t.endTime IS NOT NULL
              AND t.status = 'OPEN'
            """)
    List<TaskWatch> findPendingDeadlineNotifications();
}
