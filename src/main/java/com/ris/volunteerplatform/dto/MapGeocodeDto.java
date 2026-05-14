package com.ris.volunteerplatform.dto;

/**
 * Результат геокодирования адреса через Google Geocoding API (для карты).
 */
public record MapGeocodeDto(
        double lat,
        double lng,
        String formattedAddress
) {
}
