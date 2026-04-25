package com.ris.volunteerplatform.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MatchResultUpdateRequest(@NotNull BigDecimal matchScore) {
}
