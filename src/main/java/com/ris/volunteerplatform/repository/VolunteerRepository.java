package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByUser_Id(Long userId);

    List<Volunteer> findByActiveTrue();

    List<Volunteer> findByRegionIgnoreCase(String region);

    List<Volunteer> findByActiveTrueAndRegionIgnoreCase(String region);

    @Query("SELECT v FROM Volunteer v JOIN VolunteerSkill vs ON vs.volunteer = v WHERE vs.skill.id = :skillId")
    List<Volunteer> findBySkillId(@Param("skillId") Long skillId);

    @Query("SELECT v FROM Volunteer v JOIN VolunteerSkill vs ON vs.volunteer = v WHERE vs.skill.id = :skillId AND v.active = true")
    List<Volunteer> findActiveBySkillId(@Param("skillId") Long skillId);
}
