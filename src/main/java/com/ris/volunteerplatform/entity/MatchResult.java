package com.ris.volunteerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Сохранённый результат матчинга: пара задача–волонтёр и итоговый match_score.
 */
@Entity
@Table(name = "match_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @Column(name = "match_score", nullable = false, precision = 14, scale = 6)
    private BigDecimal matchScore;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @PrePersist
    void prePersist() {
        if (computedAt == null) {
            computedAt = Instant.now();
        }
    }
}
