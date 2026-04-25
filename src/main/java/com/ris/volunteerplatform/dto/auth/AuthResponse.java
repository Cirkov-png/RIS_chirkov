package com.ris.volunteerplatform.dto.auth;

import com.ris.volunteerplatform.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Long userId;
    private String username;
    private UserRole role;
}
