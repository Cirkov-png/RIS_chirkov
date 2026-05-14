package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.config.JwtProperties;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VolunteerRepository volunteerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        props.setExpirationMs(86400000L);
        jwtService = new JwtService(props);
        authService = new AuthService(userRepository, volunteerRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    @DisplayName("Регистрация: занятый логин → BadRequestException")
    void register_duplicateUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("u1");
        req.setEmail("a@b.ru");
        req.setPassword("p");
        when(userRepository.existsByUsername("u1")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("занято");
    }

    @Test
    @DisplayName("Регистрация волонтёра создаёт связанный Volunteer")
    void register_volunteer_createsProfile() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("vol");
        req.setEmail("vol@x.ru");
        req.setPassword("plain");
        req.setRole(UserRole.VOLUNTEER);

        when(userRepository.existsByUsername("vol")).thenReturn(false);
        when(userRepository.existsByEmail("vol@x.ru")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("hash");
        User savedUser = User.builder().id(42L).username("vol").email("vol@x.ru")
                .passwordHash("hash").role(UserRole.VOLUNTEER).enabled(true).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse res = authService.register(req);

        assertThat(res.getToken()).isNotBlank();
        assertThat(jwtService.extractUsername(res.getToken())).isEqualTo("vol");
        assertThat(res.getUserId()).isEqualTo(42L);
        ArgumentCaptor<Volunteer> vc = ArgumentCaptor.forClass(Volunteer.class);
        verify(volunteerRepository).save(vc.capture());
        assertThat(vc.getValue().getUser()).isSameAs(savedUser);
        assertThat(vc.getValue().isActive()).isTrue();
    }

    @Test
    @DisplayName("Успешный логин возвращает JWT")
    void login_ok() {
        LoginRequest req = new LoginRequest("u", "p");
        User user = User.builder().id(1L).username("u").email("e@e.ru")
                .passwordHash("h").role(UserRole.ORGANIZER).enabled(true).build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));

        AuthResponse res = authService.login(req);

        assertThat(res.getToken()).isNotBlank();
        assertThat(jwtService.extractUsername(res.getToken())).isEqualTo("u");
        assertThat(res.getUsername()).isEqualTo("u");
    }
}
