package com.ris.volunteerplatform.dto;

import com.ris.volunteerplatform.entity.UserRole;
import jakarta.validation.constraints.Email;

public record UserUpdateRequest(
        String username,
        @Email String email,
        String password,
        UserRole role,
        Boolean enabled
) {
}
