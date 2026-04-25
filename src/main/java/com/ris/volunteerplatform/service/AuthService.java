package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.auth.AuthResponse;
import com.ris.volunteerplatform.dto.auth.LoginRequest;
import com.ris.volunteerplatform.dto.auth.RegisterRequest;
import com.ris.volunteerplatform.entity.User;
import com.ris.volunteerplatform.entity.UserRole;
import com.ris.volunteerplatform.entity.Volunteer;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.repository.UserRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import com.ris.volunteerplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Регистрация и выдача JWT после проверки пароля.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Имя пользователя уже занято");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email уже зарегистрирован");
        }
        UserRole role = request.getRole() != null ? request.getRole() : UserRole.VOLUNTEER;
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        if (role == UserRole.VOLUNTEER) {
            volunteerRepository.save(Volunteer.builder()
                    .user(user)
                    .fullName(request.getUsername())
                    .active(true)
                    .build());
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Неверные учётные данные"));
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
