package com.ris.volunteerplatform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record VolunteerSkillRequest(
        @NotNull Long volunteerId,
        @NotNull Long skillId,
        @Min(1) @Max(5) Integer proficiencyLevel
) {
}
