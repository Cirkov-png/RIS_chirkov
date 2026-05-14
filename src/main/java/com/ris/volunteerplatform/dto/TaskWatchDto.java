package com.ris.volunteerplatform.dto;

import java.time.Instant;
import java.util.UUID;

public record TaskWatchDto(
        UUID uuid,
        UUID volunteerUuid,
        UUID taskUuid,
        String taskTitle,
        String taskStatus,
        Instant taskEndTime,
        Instant watchedAt,
        boolean notifiedDeadline,
        long daysUntilDeadline
) {
}
