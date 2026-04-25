package com.ris.volunteerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Требование задачи к навыку: вес importance_weight задаёт значимость при ранжировании.
 */
@Entity
@Table(name = "task_requirements", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"task_id", "skill_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    /**
     * Положительный вес важности навыка для данной задачи.
     */
    @Column(name = "importance_weight", nullable = false, precision = 12, scale = 4)
    private BigDecimal importanceWeight;
}
