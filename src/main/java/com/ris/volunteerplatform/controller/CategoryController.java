package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.CategoryDto;
import com.ris.volunteerplatform.dto.CategoryRequest;
import com.ris.volunteerplatform.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> list() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public CategoryDto get(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER')")
    public CategoryDto create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER')")
    public CategoryDto update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('COORDINATOR')")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
