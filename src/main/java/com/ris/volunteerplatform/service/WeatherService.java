package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.WeatherDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * Внешний API #1: OpenWeatherMap — погода по городу/региону для задачи.
 * Документация: https://openweathermap.org/api/one-call-3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final RestTemplate restTemplate;

    @Value("${app.weather.api-key:demo}")
    private String apiKey;

    @SuppressWarnings("unchecked")
    public Optional<WeatherDto> getWeatherForCity(String city) {
        if (city == null || city.isBlank() || apiKey == null || apiKey.isBlank() || "demo".equalsIgnoreCase(apiKey)) {
            log.warn("WeatherService: api-key не настроен или город пустой, возвращаем пустой результат");
            return Optional.empty();
        }
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "ru")
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return Optional.empty();

            Map<String, Object> main = (Map<String, Object>) response.get("main");
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            var weatherList = (java.util.List<Map<String, Object>>) response.get("weather");

            String description = weatherList != null && !weatherList.isEmpty()
                    ? (String) weatherList.get(0).get("description") : "";
            String icon = weatherList != null && !weatherList.isEmpty()
                    ? (String) weatherList.get(0).get("icon") : "";

            double temp = main != null ? ((Number) main.get("temp")).doubleValue() : 0;
            int humidity = main != null ? ((Number) main.get("humidity")).intValue() : 0;
            double windSpeed = wind != null ? ((Number) wind.get("speed")).doubleValue() : 0;

            return Optional.of(new WeatherDto(city, description, temp, humidity, windSpeed, icon));
        } catch (Exception e) {
            log.error("WeatherService: ошибка получения погоды для города '{}': {}", city, e.getMessage());
            return Optional.empty();
        }
    }
}
