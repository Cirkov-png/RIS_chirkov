package com.ris.volunteerplatform.dto;

import com.ris.volunteerplatform.entity.UserRole;

import java.time.Instant;

public record UserDto(
        Long id,
        String username,
        String email,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        String profileFullName,
        String profilePhone,
        String profileBio,
        String profileAvatarUrl
) {
}
