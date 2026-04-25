package com.ris.volunteerplatform.dto;

import java.math.BigDecimal;

/**
 * Результат ранжирования: волонтёр и нормализованный match_score [0; 1].
 */
public record RecommendedVolunteerDto(
        Long volunteerId,
        Long userId,
        String fullName,
        BigDecimal matchScore
) {
}
