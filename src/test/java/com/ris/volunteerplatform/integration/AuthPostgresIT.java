package com.ris.volunteerplatform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ris.volunteerplatform.dto.auth.LoginRequest;
import com.ris.volunteerplatform.dto.auth.RegisterRequest;
import com.ris.volunteerplatform.entity.UserRole;
import com.ris.volunteerplatform.support.PostgresTestcontainersBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class AuthPostgresIT extends PostgresTestcontainersBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Подсказка GET /api/auth/login доступна без токена")
    void loginHintIsPublic() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("POST"));
    }

    @Test
    @DisplayName("Защищённое API без JWT возвращает 401")
    void protectedEndpointWithoutTokenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/categories")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Регистрация и вход возвращают JWT")
    void registerThenLoginIssuedJwt() throws Exception {
        String username = "it_user_" + System.nanoTime();

        RegisterRequest reg = new RegisterRequest();
        reg.setUsername(username);
        reg.setEmail(username + "@example.org");
        reg.setPassword("secret123");
        reg.setRole(UserRole.VOLUNTEER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"));

        LoginRequest login = new LoginRequest(username, "secret123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())));
    }
}
