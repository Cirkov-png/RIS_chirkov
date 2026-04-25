package com.ris.volunteerplatform.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Закрытие одобренной заявки: факт выполнения и оценка волонтёра.
 */
public record CloseApplicationRequest(
        @NotNull Boolean successful,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating
) {
}
