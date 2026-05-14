package com.ris.volunteerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Волонтёр добавляет задачу в список отслеживания.
 * Система уведомляет когда до дедлайна осталось reminderDays дней.
 */
@Entity
@Table(name = "task_watches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"volunteer_id", "task_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "watched_at", nullable = false, updatable = false)
    private Instant watchedAt;

    @Column(name = "notified_deadline", nullable = false)
    @Builder.Default
    private boolean notifiedDeadline = false;

    @PrePersist
    void prePersist() {
        if (watchedAt == null) watchedAt = Instant.now();
        if (uuid == null) uuid = UUID.randomUUID();
    }
}
