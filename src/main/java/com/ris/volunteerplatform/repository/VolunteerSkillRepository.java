package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.VolunteerSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VolunteerSkillRepository extends JpaRepository<VolunteerSkill, Long> {
    List<VolunteerSkill> findByVolunteerId(Long volunteerId);

    void deleteByVolunteerIdAndSkillId(Long volunteerId, Long skillId);
}
