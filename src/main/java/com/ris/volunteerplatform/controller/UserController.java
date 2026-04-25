package com.ris.volunteerplatform.controller;

import com.ris.volunteerplatform.dto.UserCreateRequest;
import com.ris.volunteerplatform.dto.UserDto;
import com.ris.volunteerplatform.dto.UserProfilePatchRequest;
import com.ris.volunteerplatform.dto.UserUpdateRequest;
import com.ris.volunteerplatform.security.SecurityUserDetails;
import com.ris.volunteerplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD пользователей (администрирование — координатор).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER')")
    public List<UserDto> list() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR','ORGANIZER','VOLUNTEER','ADMIN')")
    public UserDto get(@PathVariable Long id) {
        return userService.findById(id);
    }

    /** Организатор обновляет публичную карточку (ФИО, телефон, о себе, URL аватара). */
    @PatchMapping("/me/profile")
    @PreAuthorize("hasRole('ORGANIZER')")
    public UserDto patchMyProfile(Authentication authentication, @RequestBody UserProfilePatchRequest request) {
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUserDetails sud)) {
            throw new AccessDeniedException("Требуется вход в систему");
        }
        return userService.patchPublicProfile(sud.getUser().getId(), request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COORDINATOR')")
    public UserDto create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('COORDINATOR')")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
