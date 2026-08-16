package com.spms.userservice.service;

import com.spms.userservice.dto.*;
import com.spms.userservice.entity.User;
import com.spms.userservice.entity.UserActivityLog;
import com.spms.userservice.exception.InvalidCredentialsException;
import com.spms.userservice.exception.ResourceNotFoundException;
import com.spms.userservice.exception.UserAlreadyExistsException;
import com.spms.userservice.repository.UserActivityLogRepository;
import com.spms.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserActivityLogRepository activityLogRepository;

    @Override
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);

        logActivity(savedUser.getId(), "USER_REGISTRATION",
                "User registered with role: " + savedUser.getRole());

        return mapToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(UserLoginRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        logActivity(user.getId(), "USER_LOGIN", "User logged in successfully");

        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());
        }

        User updatedUser = userRepository.save(user);
        logActivity(updatedUser.getId(), "PROFILE_UPDATE", "User profile updated");

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityLogResponse> getUserHistory(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        return activityLogRepository.findByUserIdOrderByTimestampDesc(id).stream()
                .map(logItem -> UserActivityLogResponse.builder()
                        .id(logItem.getId())
                        .userId(logItem.getUserId())
                        .activityType(logItem.getActivityType())
                        .description(logItem.getDescription())
                        .timestamp(logItem.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void logActivity(Long userId, String activityType, String description) {
        UserActivityLog activityLog = UserActivityLog.builder()
                .userId(userId)
                .activityType(activityType)
                .description(description)
                .build();
        activityLogRepository.save(activityLog);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
