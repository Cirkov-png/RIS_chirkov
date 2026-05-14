package com.ris.volunteerplatform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ris.volunteerplatform.config.JwtProperties;
import com.ris.volunteerplatform.controller.AuthController;
import com.ris.volunteerplatform.dto.auth.AuthResponse;
import com.ris.volunteerplatform.dto.auth.RegisterRequest;
import com.ris.volunteerplatform.entity.UserRole;
import com.ris.volunteerplatform.repository.UserRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import com.ris.volunteerplatform.security.JwtService;
import com.ris.volunteerplatform.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Слой HTTP без Spring-контекста. AuthService — подкласс с фиктивным register (без Mockito для сервиса).
 */
class AuthControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static JwtProperties testJwtProps() {
        JwtProperties p = new JwtProperties();
        p.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        p.setExpirationMs(86400000L);
        return p;
    }

    @BeforeEach
    void setUp() {
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        VolunteerRepository volunteerRepository = org.mockito.Mockito.mock(VolunteerRepository.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
        JwtService jwtService = new JwtService(testJwtProps());

        AuthService authService = new AuthService(
                userRepository, volunteerRepository, passwordEncoder, jwtService, authenticationManager) {
            @Override
            public AuthResponse register(RegisterRequest request) {
                UserRole role = request.getRole() != null ? request.getRole() : UserRole.VOLUNTEER;
                return new AuthResponse("jwt-mock", 10L, request.getUsername(), role);
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    @DisplayName("GET /api/auth/login — подсказка")
    void loginHint() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("POST"));
    }

    @Test
    @DisplayName("POST /api/auth/register — делегирует в AuthService")
    void registerDelegates() throws Exception {
        RegisterRequest reg = new RegisterRequest("u1", "u1@ex.org", "secret", UserRole.VOLUNTEER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.username").value("u1"));
    }
}
