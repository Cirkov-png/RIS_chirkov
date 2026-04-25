package com.ris.volunteerplatform.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MatchResultCreateRequest(
        @NotNull Long taskId,
        @NotNull Long volunteerId,
        @NotNull BigDecimal matchScore
) {
}
