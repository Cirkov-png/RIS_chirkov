package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.UserProfilePatchRequest;
import com.ris.volunteerplatform.entity.User;
import com.ris.volunteerplatform.entity.UserRole;
import com.ris.volunteerplatform.exception.BadRequestException;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.UserRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VolunteerRepository volunteerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("findById: пользователь не найден")
    void findById_missing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("patchPublicProfile: только для ORGANIZER")
    void patchProfile_notOrganizer() {
        User u = User.builder().id(2L).role(UserRole.VOLUNTEER).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() ->
                userService.patchPublicProfile(2L, new UserProfilePatchRequest("name", null, null, null)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("patchPublicProfile: успех")
    void patchProfile_ok() {
        User u = User.builder().id(2L).role(UserRole.ORGANIZER).username("o").email("o@o.ru")
                .passwordHash("h").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        when(userRepository.save(u)).thenReturn(u);

        var dto = userService.patchPublicProfile(2L, new UserProfilePatchRequest("Иван П.", null, null, null));

        assertThat(dto.profileFullName()).isEqualTo("Иван П.");
        verify(userRepository).save(u);
    }
}
