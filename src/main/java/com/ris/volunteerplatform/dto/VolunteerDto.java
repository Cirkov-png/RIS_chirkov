package com.ris.volunteerplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VolunteerDto(
        Long id,
        Long userId,
        String fullName,
        String phone,
        String region,
        String bio,
        boolean active,
        LocalDate birthDate,
        BigDecimal rating,
        int completedTasksCount,
        String avatarUrl
) {
}
