package com.spms.userservice.controller;

import com.spms.userservice.dto.*;
import com.spms.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("Received request to register user: {}", request.getEmail());
        UserResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Received request to get user by id: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("Received request to get user by email: {}", email);
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Received request to update user: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Received request to list all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<UserActivityLogResponse>> getUserHistory(@PathVariable Long id) {
        log.info("Received request to get activity history for user: {}", id);
        List<UserActivityLogResponse> history = userService.getUserHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<UserActivityLogResponse>> getUserLogs(@PathVariable Long id) {
        return getUserHistory(id);
    }

    @PostMapping("/{id}/logs")
    public ResponseEntity<Void> logUserActivity(
            @PathVariable Long id,
            @RequestParam String activityType,
            @RequestParam String description) {
        userService.logActivity(id, activityType, description);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
