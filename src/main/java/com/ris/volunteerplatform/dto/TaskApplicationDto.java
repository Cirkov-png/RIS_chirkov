package com.ris.volunteerplatform.dto;

import com.ris.volunteerplatform.entity.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TaskApplicationDto(
        Long id,
        Long taskId,
        Long volunteerId,
        ApplicationStatus status,
        String message,
        Instant appliedAt,
        BigDecimal organizerRating,
        Boolean taskCompletedSuccessfully,
        Instant organizerReviewedAt
) {
}
