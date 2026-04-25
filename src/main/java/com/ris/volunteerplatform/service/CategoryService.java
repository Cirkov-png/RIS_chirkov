package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.CategoryDto;
import com.ris.volunteerplatform.dto.CategoryRequest;
import com.ris.volunteerplatform.entity.Category;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return categoryRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена: " + id));
    }

    @Transactional
    public CategoryDto create(CategoryRequest req) {
        Category c = Category.builder().name(req.name()).description(req.description()).build();
        return toDto(categoryRepository.save(c));
    }

    @Transactional
    public CategoryDto update(Long id, CategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена: " + id));
        c.setName(req.name());
        c.setDescription(req.description());
        return toDto(categoryRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Категория не найдена: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getDescription());
    }
}
