package com.ris.volunteerplatform.dto;

import java.math.BigDecimal;

public record VolunteerStatsDto(
        Long volunteerId,
        String fullName,
        BigDecimal rating,
        int completedTasksCount,
        int pendingApplicationsCount,
        int approvedApplicationsCount
) {
}
