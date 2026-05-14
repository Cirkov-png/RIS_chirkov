package com.ris.volunteerplatform.dto;

import com.ris.volunteerplatform.entity.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskDto(
        Long id,
        UUID uuid,
        String title,
        String description,
        Long organizerId,
        Long categoryId,
        TaskStatus status,
        String location,
        Instant startTime,
        Instant endTime,
        Instant createdAt
) {
}
