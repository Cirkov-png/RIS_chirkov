package com.ris.volunteerplatform.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Тело запроса на отклик волонтёра на задачу.
 */
public record ApplyToTaskRequest(
        @NotNull Long taskId,
        String message
) {
}
