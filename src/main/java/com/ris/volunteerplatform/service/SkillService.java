package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.SkillDto;
import com.ris.volunteerplatform.dto.SkillRequest;
import com.ris.volunteerplatform.entity.Skill;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.CategoryRepository;
import com.ris.volunteerplatform.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<SkillDto> findAll() {
        return skillRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SkillDto findById(Long id) {
        return skillRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Навык не найден: " + id));
    }

    @Transactional
    public SkillDto create(SkillRequest req) {
        Skill.SkillBuilder b = Skill.builder().name(req.name());
        if (req.categoryId() != null) {
            b.category(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена: " + req.categoryId())));
        }
        return toDto(skillRepository.save(b.build()));
    }

    @Transactional
    public SkillDto update(Long id, SkillRequest req) {
        Skill s = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Навык не найден: " + id));
        s.setName(req.name());
        if (req.categoryId() == null) {
            s.setCategory(null);
        } else {
            s.setCategory(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена: " + req.categoryId())));
        }
        return toDto(skillRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Навык не найден: " + id);
        }
        skillRepository.deleteById(id);
    }

    private SkillDto toDto(Skill s) {
        return new SkillDto(s.getId(), s.getName(), s.getCategory() != null ? s.getCategory().getId() : null);
    }
}
