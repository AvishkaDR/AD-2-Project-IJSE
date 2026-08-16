package com.spms.vehicleservice.controller;

import com.spms.vehicleservice.dto.*;
import com.spms.vehicleservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> registerVehicle(
            @Valid @RequestBody VehicleRegisterRequest request) {
        log.info("Received request to register vehicle: {}", request.getRegistrationNumber());
        VehicleResponse response = vehicleService.registerVehicle(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        log.info("Received request to get vehicle by ID: {}", id);
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/registration/{registrationNumber}")
    public ResponseEntity<VehicleResponse> getVehicleByRegistration(
            @PathVariable String registrationNumber) {
        log.info("Received request to get vehicle by registration: {}", registrationNumber);
        VehicleResponse response = vehicleService.getVehicleByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles(
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            log.info("Received request to get vehicles for user ID: {}", userId);
            List<VehicleResponse> list = vehicleService.getVehiclesByUserId(userId);
            return ResponseEntity.ok(list);
        }
        log.info("Received request to list all vehicles");
        List<VehicleResponse> list = vehicleService.getAllVehicles();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByUserId(@PathVariable Long userId) {
        log.info("Received request to get vehicles for user ID: {}", userId);
        List<VehicleResponse> list = vehicleService.getVehiclesByUserId(userId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        log.info("Received request to update vehicle ID: {}", id);
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        log.info("Received request to delete vehicle ID: {}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/entry")
    public ResponseEntity<VehicleLogResponse> simulateEntry(
            @Valid @RequestBody VehicleEntryRequest request) {
        log.info("Received request to simulate vehicle entry: {}", request);
        VehicleLogResponse response = vehicleService.simulateEntry(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/entry")
    public ResponseEntity<VehicleLogResponse> simulateEntryForVehicle(
            @PathVariable Long id,
            @RequestParam Long parkingSpaceId) {
        VehicleEntryRequest request = VehicleEntryRequest.builder()
                .vehicleId(id)
                .parkingSpaceId(parkingSpaceId)
                .build();
        VehicleLogResponse response = vehicleService.simulateEntry(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/exit")
    public ResponseEntity<VehicleLogResponse> simulateExit(
            @Valid @RequestBody VehicleExitRequest request) {
        log.info("Received request to simulate vehicle exit: {}", request);
        VehicleLogResponse response = vehicleService.simulateExit(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/exit")
    public ResponseEntity<VehicleLogResponse> simulateExitForVehicle(
            @PathVariable Long id,
            @RequestParam(required = false) Long parkingSpaceId) {
        VehicleExitRequest request = VehicleExitRequest.builder()
                .vehicleId(id)
                .parkingSpaceId(parkingSpaceId)
                .build();
        VehicleLogResponse response = vehicleService.simulateExit(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<VehicleLogResponse>> getLogsByVehicleId(@PathVariable Long id) {
        log.info("Received request to get logs for vehicle ID: {}", id);
        List<VehicleLogResponse> logs = vehicleService.getLogsByVehicleId(id);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{userId}/logs")
    public ResponseEntity<List<VehicleLogResponse>> getLogsByUserId(@PathVariable Long userId) {
        log.info("Received request to get logs for user ID: {}", userId);
        List<VehicleLogResponse> logs = vehicleService.getLogsByUserId(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<VehicleLogResponse>> getAllLogs() {
        log.info("Received request to get all entry/exit logs");
        List<VehicleLogResponse> logs = vehicleService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
}
