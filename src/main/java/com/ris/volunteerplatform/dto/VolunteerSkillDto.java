package com.ris.volunteerplatform.dto;

public record VolunteerSkillDto(
        Long id,
        Long volunteerId,
        Long skillId,
        String skillName,
        String categoryName,
        int proficiencyLevel
) {
}
