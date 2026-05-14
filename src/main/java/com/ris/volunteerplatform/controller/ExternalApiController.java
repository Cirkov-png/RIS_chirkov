package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.MapGeocodeDto;
import com.ris.volunteerplatform.dto.WeatherDto;
import com.ris.volunteerplatform.service.GoogleGeocodeService;
import com.ris.volunteerplatform.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
public class ExternalApiController {

    private final WeatherService weatherService;
    private final GoogleGeocodeService googleGeocodeService;

    /**
     * Внешний API: OpenWeatherMap — погода по городу.
     * GET /api/external/weather?city=Минск
     */
    @GetMapping("/weather")
    public ResponseEntity<WeatherDto> getWeather(@RequestParam String city) {
        return weatherService.getWeatherForCity(city)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Внешний API: Google Geocoding — адрес → координаты для карты.
     * GET /api/external/maps/geocode?address=Минск,+проспект+Независимости+10
     */
    @GetMapping("/maps/geocode")
    public ResponseEntity<MapGeocodeDto> geocode(@RequestParam String address) {
        return googleGeocodeService.geocode(address)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
