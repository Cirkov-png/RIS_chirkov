package com.ris.volunteerplatform.dto;

import java.util.UUID;

public record SkillDto(Long id, UUID uuid, String name, Long categoryId) {
}
