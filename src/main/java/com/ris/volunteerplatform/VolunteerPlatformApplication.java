package com.ris.volunteerplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа Spring Boot: платформа волонтёров с модулем интеллектуального матчинга.
 */
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class VolunteerPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(VolunteerPlatformApplication.class, args);
    }
}
