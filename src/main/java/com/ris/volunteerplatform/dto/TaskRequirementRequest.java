package com.ris.volunteerplatform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TaskRequirementRequest(
        @NotNull Long taskId,
        @NotNull Long skillId,
        @NotNull @Positive BigDecimal importanceWeight
) {
}
