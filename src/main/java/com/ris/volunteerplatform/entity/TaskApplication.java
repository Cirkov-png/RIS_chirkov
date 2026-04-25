package com.ris.volunteerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Заявка волонтёра на задачу (отклик). Таблица в БД: applications.
 */
@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private Instant appliedAt;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber = 1;

    @Column(name = "organizer_rating", precision = 5, scale = 2)
    private BigDecimal organizerRating;

    @Column(name = "task_completed_successfully")
    private Boolean taskCompletedSuccessfully;

    @Column(name = "organizer_reviewed_at")
    private Instant organizerReviewedAt;

    @PrePersist
    void prePersist() {
        if (appliedAt == null) {
            appliedAt = Instant.now();
        }
    }
}
