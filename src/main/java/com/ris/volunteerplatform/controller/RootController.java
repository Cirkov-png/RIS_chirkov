package com.ris.volunteerplatform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Корень сервера: без JWT открывается в браузере (иначе Spring Security отдаёт 403 на {@code /}).
 */
@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "service", "RIS Volunteer Platform API",
                "version", "1.0",
                "endpoints", Map.of(
                        "register", "POST /api/auth/register",
                        "login", "POST /api/auth/login",
                        "api", "остальные пути — с заголовком Authorization: Bearer <JWT>"
                )
        );
    }
}
