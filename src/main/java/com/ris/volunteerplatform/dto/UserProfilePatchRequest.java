package com.ris.volunteerplatform.dto;

/**
 * Частичное обновление публичного профиля (организатор редактирует свою карточку).
 */
public record UserProfilePatchRequest(
        String profileFullName,
        String profilePhone,
        String profileBio,
        String profileAvatarUrl
) {
}
