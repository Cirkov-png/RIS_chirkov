package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.SkillRequest;
import com.ris.volunteerplatform.entity.Category;
import com.ris.volunteerplatform.entity.Skill;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.CategoryRepository;
import com.ris.volunteerplatform.repository.SkillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SkillService skillService;

    @Test
    @DisplayName("create: неверный categoryId → ResourceNotFoundException")
    void create_badCategory() {
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.create(new SkillRequest("SQL", 9L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create без категории сохраняет навык")
    void create_withoutCategory() {
        when(skillRepository.save(any(Skill.class))).thenAnswer(i -> {
            Skill s = i.getArgument(0);
            s.setId(3L);
            return s;
        });

        var dto = skillService.create(new SkillRequest("Коммуникация", null));

        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.name()).isEqualTo("Коммуникация");
        verify(skillRepository).save(any(Skill.class));
    }
}
