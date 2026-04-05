package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.UserUpdateRequest;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.entity.Role;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .role(Role.VIEWER)
                .active(true)
                .build();
    }

    @Test
    void getAllUsers_returnsListOfUserResponses() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));
        List<UserResponse> result = userService.getAllUsers();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserById_existingId_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        UserResponse result = userService.getUserById(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserById_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_roleChange_updatesRole() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setRole(Role.ANALYST);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenReturn(sampleUser);

        UserResponse result = userService.updateUser(1L, req);
        assertThat(sampleUser.getRole()).isEqualTo(Role.ANALYST);
    }

    @Test
    void deleteUser_softDeletesByDeactivating() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenReturn(sampleUser);

        userService.deleteUser(1L);

        assertThat(sampleUser.isActive()).isFalse();
        verify(userRepository).save(sampleUser);
    }
}
