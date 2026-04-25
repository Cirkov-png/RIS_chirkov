package com.ris.volunteerplatform.dto.auth;

import com.ris.volunteerplatform.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Регистрация: роль по умолчанию VOLUNTEER (координаторов обычно заводят отдельно).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    /** Необязательно: при null в сервисе подставляется VOLUNTEER */
    private UserRole role;
}
