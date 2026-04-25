package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.*;
import com.ris.volunteerplatform.service.VolunteerService;
import com.ris.volunteerplatform.service.VolunteerSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;
    private final VolunteerSkillService volunteerSkillService;

    /** UC1: Все волонтёры */
    @GetMapping
    public List<VolunteerDto> list() {
        return volunteerService.findAll();
    }

    /** UC2: Только активные */
    @GetMapping("/active")
    public List<VolunteerDto> listActive() {
        return volunteerService.findAllActive();
    }

    /** UC3: Профиль по id */
    @GetMapping("/{id}")
    public VolunteerDto get(@PathVariable Long id) {
        return volunteerService.findById(id);
    }

    /** UC4: Профиль по userId */
    @GetMapping("/by-user/{userId}")
    public VolunteerDto getByUserId(@PathVariable Long userId) {
        return volunteerService.findByUserId(userId);
    }

    /** UC5: Создание профиля */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COORDINATOR','VOLUNTEER','ADMIN')")
    public VolunteerDto create(@Valid @RequestBody VolunteerRequest request) {
        return volunteerService.create(request);
    }

    /** UC6: Редактирование профиля */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR','VOLUNTEER','ADMIN')")
    public VolunteerDto update(@PathVariable Long id, @Valid @RequestBody VolunteerRequest request) {
        return volunteerService.update(id, request);
    }

    /** UC7: Деактивация */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public VolunteerDto deactivate(@PathVariable Long id) {
        return volunteerService.deactivate(id);
    }

    /** UC8: Активация */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public VolunteerDto activate(@PathVariable Long id) {
        return volunteerService.activate(id);
    }

    /** UC9: Мои заявки */
    @GetMapping("/{id}/applications")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ORGANIZER','ADMIN')")
    public List<TaskApplicationDto> myApplications(@PathVariable Long id) {
        return volunteerService.getMyApplications(id);
    }

    /** UC10: Отзыв заявки */
    @PatchMapping("/{id}/applications/{applicationId}/withdraw")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public TaskApplicationDto withdrawApplication(@PathVariable Long id,
                                                   @PathVariable Long applicationId) {
        return volunteerService.withdrawApplication(id, applicationId);
    }

    /** UC11: Одобренные задачи */
    @GetMapping("/{id}/tasks/approved")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ORGANIZER','ADMIN')")
    public List<TaskDto> myApprovedTasks(@PathVariable Long id) {
        return volunteerService.getMyApprovedTasks(id);
    }

    /** UC12: Удаление профиля */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('COORDINATOR','ADMIN')")
    public void delete(@PathVariable Long id) {
        volunteerService.delete(id);
    }

    /** UC13: Поиск по региону */
    @GetMapping("/search/region")
    public List<VolunteerDto> findByRegion(@RequestParam String region,
                                            @RequestParam(defaultValue = "true") boolean onlyActive) {
        return volunteerService.findByRegion(region, onlyActive);
    }

    /** UC14: Поиск по навыку */
    @GetMapping("/search/skill")
    public List<VolunteerDto> findBySkill(@RequestParam Long skillId,
                                           @RequestParam(defaultValue = "true") boolean onlyActive) {
        return volunteerService.findBySkill(skillId, onlyActive);
    }

    /** UC15: Оценить волонтёра */
    @PatchMapping("/{id}/rate")
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR','ADMIN')")
    public VolunteerDto rate(@PathVariable Long id,
                              @Valid @RequestBody RateVolunteerRequest request) {
        return volunteerService.rateVolunteer(id, request.rating());
    }

    /** UC16: Статистика */
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ORGANIZER','ADMIN')")
    public VolunteerStatsDto stats(@PathVariable Long id) {
        return volunteerService.getStats(id);
    }

    /** UC17: Отметить задачу выполненной */
    @PatchMapping("/{id}/applications/{applicationId}/complete")
    @PreAuthorize("hasAnyRole('ORGANIZER','COORDINATOR','ADMIN')")
    public VolunteerDto markCompleted(@PathVariable Long id,
                                       @PathVariable Long applicationId) {
        return volunteerService.markTaskCompleted(id, applicationId);
    }

    /** UC18: Подать заявку на задачу */
    @PostMapping("/{id}/apply")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public TaskApplicationDto apply(@PathVariable Long id,
                                    @Valid @RequestBody ApplyToTaskRequest body) {
        return volunteerService.applyToTask(id, body.taskId(), body.message());
    }

    /** UC19: Доступные (открытые) задачи */
    @GetMapping("/tasks/available")
    public List<TaskDto> availableTasks() {
        return volunteerService.getAvailableTasks();
    }

    /** Статус заявки волонтёра на конкретную задачу */
    @GetMapping("/{id}/tasks/{taskId}/application")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ORGANIZER','ADMIN')")
    public org.springframework.http.ResponseEntity<TaskApplicationDto> applicationForTask(
            @PathVariable Long id, @PathVariable Long taskId) {
        return volunteerService.getApplicationForTask(id, taskId)
                .map(org.springframework.http.ResponseEntity::ok)
                .orElse(org.springframework.http.ResponseEntity.noContent().build());
    }

    /** UC20: История отклонённых заявок */
    @GetMapping("/{id}/applications/rejected")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ORGANIZER','ADMIN')")
    public List<TaskApplicationDto> rejectedApplications(@PathVariable Long id) {
        return volunteerService.getRejectedApplications(id);
    }

    /** UC21: Обновить аватарку */
    @PatchMapping("/{id}/avatar")
    @PreAuthorize("hasAnyRole('VOLUNTEER','COORDINATOR','ADMIN')")
    public VolunteerDto updateAvatar(@PathVariable Long id,
                                      @RequestBody Map<String, String> body) {
        return volunteerService.updateAvatar(id, body.get("avatarUrl"));
    }

    /** UC22: Навыки волонтёра с названиями */
    @GetMapping("/{id}/skills")
    public List<VolunteerSkillDto> mySkills(@PathVariable Long id) {
        return volunteerSkillService.findByVolunteerId(id);
    }
}
