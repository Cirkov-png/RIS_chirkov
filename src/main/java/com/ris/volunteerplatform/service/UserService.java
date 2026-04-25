package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.UserCreateRequest;
import com.ris.volunteerplatform.dto.UserDto;
import com.ris.volunteerplatform.dto.UserProfilePatchRequest;
import com.ris.volunteerplatform.dto.UserUpdateRequest;
import com.ris.volunteerplatform.entity.User;
import com.ris.volunteerplatform.entity.UserRole;
import com.ris.volunteerplatform.entity.Volunteer;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.UserRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
    }

    @Transactional
    public UserDto create(UserCreateRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BadRequestException("Имя пользователя уже занято");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("Email уже зарегистрирован");
        }
        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(req.role())
                .enabled(req.enabled())
                .build();
        user = userRepository.save(user);
        if (user.getRole() == UserRole.VOLUNTEER) {
            volunteerRepository.save(Volunteer.builder()
                    .user(user)
                    .fullName(user.getUsername())
                    .active(true)
                    .build());
        }
        return toDto(user);
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
        if (req.username() != null && !req.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(req.username())) {
                throw new BadRequestException("Имя пользователя уже занято");
            }
            user.setUsername(req.username());
        }
        if (req.email() != null && !req.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.email())) {
                throw new BadRequestException("Email уже зарегистрирован");
            }
            user.setEmail(req.email());
        }
        if (req.password() != null && !req.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        if (req.role() != null) {
            user.setRole(req.role());
        }
        if (req.enabled() != null) {
            user.setEnabled(req.enabled());
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь не найден: " + id);
        }
        userRepository.deleteById(id);
    }

    /** Публичный профиль на карточке организатора (свои поля). */
    @Transactional
    public UserDto patchPublicProfile(Long userId, UserProfilePatchRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));
        if (user.getRole() != UserRole.ORGANIZER) {
            throw new BadRequestException("Публичный профиль редактируют только организаторы");
        }
        if (req.profileFullName() != null) {
            user.setProfileFullName(trimToNull(req.profileFullName()));
        }
        if (req.profilePhone() != null) {
            user.setProfilePhone(trimToNull(req.profilePhone()));
        }
        if (req.profileBio() != null) {
            user.setProfileBio(trimToNull(req.profileBio()));
        }
        if (req.profileAvatarUrl() != null) {
            user.setProfileAvatarUrl(trimToNull(req.profileAvatarUrl()));
        }
        return toDto(userRepository.save(user));
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole(),
                u.isEnabled(),
                u.getCreatedAt(),
                u.getProfileFullName(),
                u.getProfilePhone(),
                u.getProfileBio(),
                u.getProfileAvatarUrl());
    }
}
