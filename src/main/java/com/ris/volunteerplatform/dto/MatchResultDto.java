package com.ris.volunteerplatform.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MatchResultDto(Long id, Long taskId, Long volunteerId, BigDecimal matchScore, Instant computedAt) {
}
