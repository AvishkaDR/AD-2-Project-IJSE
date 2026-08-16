package com.spms.userservice.service;

import com.spms.userservice.dto.*;

import java.util.List;

public interface UserService {

    UserResponse register(UserRegisterRequest request);

    UserResponse login(UserLoginRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    List<UserResponse> getAllUsers();

    List<UserActivityLogResponse> getUserHistory(Long id);

    void logActivity(Long userId, String activityType, String description);
}
