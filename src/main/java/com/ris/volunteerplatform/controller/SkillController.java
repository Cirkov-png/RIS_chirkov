package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.SkillDto;
import com.ris.volunteerplatform.dto.SkillRequest;
import com.ris.volunteerplatform.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public List<SkillDto> list() {
        return skillService.findAll();
    }

    @GetMapping("/{id}")
    public SkillDto get(@PathVariable Long id) {
        return skillService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER')")
    public SkillDto create(@Valid @RequestBody SkillRequest request) {
        return skillService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER')")
    public SkillDto update(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return skillService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('COORDINATOR')")
    public void delete(@PathVariable Long id) {
        skillService.delete(id);
    }
}
