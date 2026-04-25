    package com.ris.volunteerplatform.controller;
    
    import com.ris.volunteerplatform.dto.auth.AuthResponse;
    import com.ris.volunteerplatform.dto.auth.LoginRequest;
    import com.ris.volunteerplatform.dto.auth.RegisterRequest;
    import com.ris.volunteerplatform.service.AuthService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;
    
    import java.util.Map;
    
    /**
     * Публичные эндпоинты: регистрация и получение JWT.
     */
    @RestController
    @RequestMapping("/api/auth")
    @RequiredArgsConstructor
    public class AuthController {
    
        private final AuthService authService;
    
        /**
         * Браузер открывает URL методом GET; вход только через POST — иначе был бы 405 и Whitelabel Error Page.
         */
        @GetMapping("/login")
        public Map<String, Object> loginHint() {
            return Map.of(
                    "message", "Используйте POST с JSON-телом и заголовком Content-Type: application/json",
                    "method", "POST",
                    "bodyExample", Map.of("username", "логин", "password", "пароль"));
        }
    
        @GetMapping("/register")
        public Map<String, Object> registerHint() {
            return Map.of(
                    "message", "Используйте POST с JSON-телом и заголовком Content-Type: application/json",
                    "method", "POST",
                    "bodyExample", Map.of(
                            "username", "логин",
                            "email", "email@example.com",
                            "password", "пароль",
                            "role", "VOLUNTEER (необязательно; по умолчанию VOLUNTEER)"));
        }
    
        @PostMapping("/register")
        @ResponseStatus(HttpStatus.CREATED)
        public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
            return authService.register(request);
        }
    
        @PostMapping("/login")
        public AuthResponse login(@Valid @RequestBody LoginRequest request) {
            return authService.login(request);
        }
    }
