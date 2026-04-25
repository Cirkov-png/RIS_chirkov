package com.ris.volunteerplatform.dto;

import com.ris.volunteerplatform.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TaskRequest(
        @NotBlank String title,
        String description,
        Long categoryId,
        @NotNull TaskStatus status,
        String location,
        Instant startTime,
        Instant endTime,
        /** Если задано и вызывающий — COORDINATOR: задача привязывается к этому организатору. */
        Long organizerUserId
) {
}
