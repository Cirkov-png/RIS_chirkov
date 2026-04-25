package com.ris.volunteerplatform.dto;

import com.ris.volunteerplatform.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull UserRole role,
        boolean enabled
) {
}
