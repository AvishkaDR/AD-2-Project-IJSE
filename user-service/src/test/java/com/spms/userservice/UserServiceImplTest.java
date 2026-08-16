package com.spms.userservice;

import com.spms.userservice.dto.UserRegisterRequest;
import com.spms.userservice.dto.UserResponse;
import com.spms.userservice.entity.User;
import com.spms.userservice.entity.UserRole;
import com.spms.userservice.repository.UserActivityLogRepository;
import com.spms.userservice.repository.UserRepository;
import com.spms.userservice.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActivityLogRepository activityLogRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("secret123")
                .role(UserRole.DRIVER)
                .phoneNumber("1234567890")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testRegisterUser_Success() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("secret123")
                .role(UserRole.DRIVER)
                .phoneNumber("1234567890")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
        assertEquals("John Doe", response.getName());
        assertEquals(UserRole.DRIVER, response.getRole());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
    }
}
