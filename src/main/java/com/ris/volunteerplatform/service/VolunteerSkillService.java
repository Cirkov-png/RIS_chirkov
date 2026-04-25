package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.VolunteerSkillDto;
import com.ris.volunteerplatform.dto.VolunteerSkillRequest;
import com.ris.volunteerplatform.entity.VolunteerSkill;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.SkillRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import com.ris.volunteerplatform.repository.VolunteerSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolunteerSkillService {

    private final VolunteerSkillRepository volunteerSkillRepository;
    private final VolunteerRepository volunteerRepository;
    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<VolunteerSkillDto> findAll() {
        return volunteerSkillRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<VolunteerSkillDto> findByVolunteerId(Long volunteerId) {
        return volunteerSkillRepository.findByVolunteerId(volunteerId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public VolunteerSkillDto findById(Long id) {
        return volunteerSkillRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Запись volunteer_skills не найдена: " + id));
    }

    @Transactional
    public VolunteerSkillDto create(VolunteerSkillRequest req) {
        var volunteer = volunteerRepository.findById(req.volunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + req.volunteerId()));
        var skill = skillRepository.findById(req.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Навык не найден: " + req.skillId()));
        int level = req.proficiencyLevel() != null ? req.proficiencyLevel() : 3;
        VolunteerSkill vs = VolunteerSkill.builder()
                .volunteer(volunteer)
                .skill(skill)
                .proficiencyLevel(level)
                .build();
        return toDto(volunteerSkillRepository.save(vs));
    }

    @Transactional
    public VolunteerSkillDto update(Long id, VolunteerSkillRequest req) {
        VolunteerSkill vs = volunteerSkillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись volunteer_skills не найдена: " + id));
        vs.setVolunteer(volunteerRepository.findById(req.volunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + req.volunteerId())));
        vs.setSkill(skillRepository.findById(req.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Навык не найден: " + req.skillId())));
        if (req.proficiencyLevel() != null) {
            vs.setProficiencyLevel(req.proficiencyLevel());
        }
        return toDto(volunteerSkillRepository.save(vs));
    }

    @Transactional
    public void delete(Long id) {
        if (!volunteerSkillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Запись volunteer_skills не найдена: " + id);
        }
        volunteerSkillRepository.deleteById(id);
    }

    private VolunteerSkillDto toDto(VolunteerSkill vs) {
        return new VolunteerSkillDto(
                vs.getId(),
                vs.getVolunteer().getId(),
                vs.getSkill().getId(),
                vs.getSkill().getName(),
                vs.getSkill().getCategory() != null ? vs.getSkill().getCategory().getName() : null,
                vs.getProficiencyLevel());
    }
}
