package com.ris.volunteerplatform.dto;

public record WeatherDto(
        String city,
        String description,
        double temperatureCelsius,
        int humidity,
        double windSpeed,
        String icon
) {
}
