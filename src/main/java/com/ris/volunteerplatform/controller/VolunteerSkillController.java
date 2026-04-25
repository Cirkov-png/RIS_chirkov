package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.VolunteerSkillDto;
import com.ris.volunteerplatform.dto.VolunteerSkillRequest;
import com.ris.volunteerplatform.service.VolunteerSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volunteer-skills")
@RequiredArgsConstructor
public class VolunteerSkillController {

    private final VolunteerSkillService volunteerSkillService;

    @GetMapping
    public List<VolunteerSkillDto> list() {
        return volunteerSkillService.findAll();
    }

    @GetMapping("/{id}")
    public VolunteerSkillDto get(@PathVariable Long id) {
        return volunteerSkillService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COORDINATOR','VOLUNTEER')")
    public VolunteerSkillDto create(@Valid @RequestBody VolunteerSkillRequest request) {
        return volunteerSkillService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR','VOLUNTEER')")
    public VolunteerSkillDto update(@PathVariable Long id, @Valid @RequestBody VolunteerSkillRequest request) {
        return volunteerSkillService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('COORDINATOR','VOLUNTEER')")
    public void delete(@PathVariable Long id) {
        volunteerSkillService.delete(id);
    }
}
