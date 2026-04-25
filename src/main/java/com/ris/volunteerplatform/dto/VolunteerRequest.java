package com.ris.volunteerplatform.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VolunteerRequest(
        @NotNull Long userId,
        String fullName,
        String phone,
        String region,
        String bio,
        Boolean active,
        LocalDate birthDate,
        String avatarUrl
) {
}
