package com.ris.volunteerplatform.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
        @NotBlank String name,
        Long categoryId
) {
}
