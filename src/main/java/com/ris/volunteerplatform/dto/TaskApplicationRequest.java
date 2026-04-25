package com.ris.volunteerplatform.dto;

import com.ris.volunteerplatform.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record TaskApplicationRequest(
        @NotNull Long taskId,
        @NotNull Long volunteerId,
        ApplicationStatus status,
        String message
) {
}
