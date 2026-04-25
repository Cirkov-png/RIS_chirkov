package com.ris.volunteerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Профиль волонтёра, связанный 1:1 с пользователем роли VOLUNTEER.
 */
@Entity
@Table(name = "volunteers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String region;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "completed_tasks_count", nullable = false)
    @Builder.Default
    private int completedTasksCount = 0;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    /** Сумма ручных оценок (координатор), для среднего вместе с оценками по заявкам */
    @Column(name = "manual_rating_sum", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal manualRatingSum = BigDecimal.ZERO;

    @Column(name = "manual_rating_count", nullable = false)
    @Builder.Default
    private int manualRatingCount = 0;
}
