package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}
