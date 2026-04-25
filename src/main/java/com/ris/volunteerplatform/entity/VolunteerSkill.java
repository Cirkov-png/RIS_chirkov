package com.ris.volunteerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Связь волонтёр — навык с уровнем владения (1–5), участвует в расчёте матчинга.
 */
@Entity
@Table(name = "volunteer_skills", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"volunteer_id", "skill_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    /**
     * Уровень 1 (начальный) … 5 (эксперт). Используется как множитель к весу требования.
     */
    @Column(name = "proficiency_level", nullable = false)
    private int proficiencyLevel = 3;
}
