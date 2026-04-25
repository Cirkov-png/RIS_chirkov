package com.ris.volunteerplatform.dto;

import java.math.BigDecimal;

public record TaskRequirementDto(
        Long id,
        Long taskId,
        Long skillId,
        BigDecimal importanceWeight,
        String skillName
) {
}
