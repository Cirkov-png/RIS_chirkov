package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.CategoryRequest;
import com.ris.volunteerplatform.entity.Category;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("findAll отдаёт DTO списком")
    void findAll() {
        Category c = Category.builder().id(1L).name("A").description("d").build();
        when(categoryRepository.findAll()).thenReturn(List.of(c));

        var list = categoryService.findAll();
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().name()).isEqualTo("A");
    }

    @Test
    @DisplayName("findById: нет записи → ResourceNotFoundException")
    void findById_missing() {
        when(categoryRepository.findById(55L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.findById(55L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create сохраняет сущность")
    void create() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> {
            Category x = i.getArgument(0);
            x.setId(7L);
            return x;
        });

        var dto = categoryService.create(new CategoryRequest("Новая", "описание"));

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("Новая");
    }

    @Test
    @DisplayName("delete: нет id → исключение")
    void delete_missing() {
        when(categoryRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> categoryService.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}
