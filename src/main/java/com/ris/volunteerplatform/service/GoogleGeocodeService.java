package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.MapGeocodeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Google Geocoding API — адрес → координаты для отображения на карте.
 * Документация: https://developers.google.com/maps/documentation/geocoding
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleGeocodeService {

    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    private final RestTemplate restTemplate;

    @Value("${app.google.maps-api-key:}")
    private String apiKey;

    @SuppressWarnings("unchecked")
    public Optional<MapGeocodeDto> geocode(String address) {
        if (address == null || address.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn("GoogleGeocodeService: пустой адрес или app.google.maps-api-key не задан");
            return Optional.empty();
        }
        try {
            String url = UriComponentsBuilder.fromUriString(GEOCODE_URL)
                    .queryParam("address", address)
                    .queryParam("key", apiKey)
                    .queryParam("language", "ru")
                    .encode()
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return Optional.empty();
            }
            String status = String.valueOf(response.get("status"));
            if (!"OK".equals(status)) {
                log.warn("GoogleGeocodeService: status={} для адреса «{}»", status, address);
                return Optional.empty();
            }
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                return Optional.empty();
            }
            Map<String, Object> first = results.get(0);
            String formatted = (String) first.get("formatted_address");
            Map<String, Object> geometry = (Map<String, Object>) first.get("geometry");
            if (geometry == null) {
                return Optional.empty();
            }
            Map<String, Object> location = (Map<String, Object>) geometry.get("location");
            if (location == null) {
                return Optional.empty();
            }
            double lat = ((Number) location.get("lat")).doubleValue();
            double lng = ((Number) location.get("lng")).doubleValue();
            return Optional.of(new MapGeocodeDto(lat, lng, formatted != null ? formatted : address));
        } catch (Exception e) {
            log.error("GoogleGeocodeService: ошибка для «{}»: {}", address, e.getMessage());
            return Optional.empty();
        }
    }
}
