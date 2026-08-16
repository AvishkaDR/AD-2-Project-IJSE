package com.spms.vehicleservice.service;

import com.spms.vehicleservice.client.ParkingSpaceClient;
import com.spms.vehicleservice.client.UserClient;
import com.spms.vehicleservice.dto.*;
import com.spms.vehicleservice.entity.Vehicle;
import com.spms.vehicleservice.entity.VehicleEntryExitLog;
import com.spms.vehicleservice.entity.VehicleParkingStatus;
import com.spms.vehicleservice.exception.DuplicateVehicleException;
import com.spms.vehicleservice.exception.InvalidVehicleStateException;
import com.spms.vehicleservice.exception.ResourceNotFoundException;
import com.spms.vehicleservice.repository.VehicleEntryExitLogRepository;
import com.spms.vehicleservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleEntryExitLogRepository logRepository;
    private final ParkingSpaceClient parkingSpaceClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public VehicleResponse registerVehicle(VehicleRegisterRequest request) {
        log.info("Registering vehicle: {}", request.getRegistrationNumber());
        if (vehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateVehicleException(
                    "Vehicle with registration number " + request.getRegistrationNumber() + " already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .registrationNumber(request.getRegistrationNumber().toUpperCase().trim())
                .vehicleType(request.getVehicleType())
                .userId(request.getUserId())
                .parkingStatus(VehicleParkingStatus.OUT_OF_PARKING)
                .make(request.getMake())
                .model(request.getModel())
                .color(request.getColor())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);

        try {
            userClient.logUserActivity(
                    saved.getUserId(),
                    "VEHICLE_REGISTERED",
                    "Registered vehicle " + saved.getRegistrationNumber());
        } catch (Exception e) {
            log.warn("Could not log user activity via user-service: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
        return mapToResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleByRegistrationNumber(String registrationNumber) {
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(registrationNumber.toUpperCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with registration: " + registrationNumber));
        return mapToResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request) {
        log.info("Updating vehicle with ID: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        if (request.getVehicleType() != null) vehicle.setVehicleType(request.getVehicleType());
        if (request.getMake() != null) vehicle.setMake(request.getMake());
        if (request.getModel() != null) vehicle.setModel(request.getModel());
        if (request.getColor() != null) vehicle.setColor(request.getColor());

        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with ID: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        if (vehicle.getParkingStatus() == VehicleParkingStatus.IN_PARKING) {
            throw new InvalidVehicleStateException("Cannot delete vehicle while it is parked in a space");
        }

        vehicleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public VehicleLogResponse simulateEntry(VehicleEntryRequest request) {
        log.info("Simulating vehicle entry: vehicleId={}, spaceId={}", request.getVehicleId(), request.getParkingSpaceId());
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + request.getVehicleId()));

        if (vehicle.getParkingStatus() == VehicleParkingStatus.IN_PARKING) {
            throw new InvalidVehicleStateException(
                    "Vehicle " + vehicle.getRegistrationNumber() + " is already in parking space ID: " + vehicle.getCurrentParkingSpaceId());
        }

        BigDecimal hourlyRate = BigDecimal.valueOf(10.00); // default fallback
        try {
            ParkingSpaceDto spaceDto = parkingSpaceClient.getParkingSpaceById(request.getParkingSpaceId());
            if (spaceDto != null && spaceDto.getHourlyRate() != null) {
                hourlyRate = spaceDto.getHourlyRate();
            }
            parkingSpaceClient.occupySpace(request.getParkingSpaceId());
        } catch (Exception e) {
            log.warn("Could not notify parking space service or get details: {}", e.getMessage());
        }

        LocalDateTime entryTime = request.getEntryTime() != null ? request.getEntryTime() : LocalDateTime.now();

        vehicle.setParkingStatus(VehicleParkingStatus.IN_PARKING);
        vehicle.setCurrentParkingSpaceId(request.getParkingSpaceId());
        vehicleRepository.save(vehicle);

        VehicleEntryExitLog entryLog = VehicleEntryExitLog.builder()
                .vehicleId(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .parkingSpaceId(request.getParkingSpaceId())
                .userId(vehicle.getUserId())
                .entryTime(entryTime)
                .hourlyRate(hourlyRate)
                .status("ACTIVE")
                .build();

        VehicleEntryExitLog savedLog = logRepository.save(entryLog);

        try {
            userClient.logUserActivity(
                    vehicle.getUserId(),
                    "VEHICLE_ENTRY",
                    "Vehicle " + vehicle.getRegistrationNumber() + " entered parking space " + request.getParkingSpaceId());
        } catch (Exception e) {
            log.warn("Could not log user activity: {}", e.getMessage());
        }

        return mapToLogResponse(savedLog);
    }

    @Override
    @Transactional
    public VehicleLogResponse simulateExit(VehicleExitRequest request) {
        log.info("Simulating vehicle exit: vehicleId={}", request.getVehicleId());
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + request.getVehicleId()));

        if (vehicle.getParkingStatus() != VehicleParkingStatus.IN_PARKING) {
            throw new InvalidVehicleStateException(
                    "Vehicle " + vehicle.getRegistrationNumber() + " is not currently parked");
        }

        Long parkingSpaceId = request.getParkingSpaceId() != null ?
                request.getParkingSpaceId() : vehicle.getCurrentParkingSpaceId();

        try {
            if (parkingSpaceId != null) {
                parkingSpaceClient.vacateSpace(parkingSpaceId);
            }
        } catch (Exception e) {
            log.warn("Could not notify parking space service: {}", e.getMessage());
        }

        LocalDateTime exitTime = request.getExitTime() != null ? request.getExitTime() : LocalDateTime.now();

        VehicleEntryExitLog activeLog = logRepository
                .findFirstByVehicleIdAndStatusOrderByCreatedAtDesc(vehicle.getId(), "ACTIVE")
                .orElseGet(() -> VehicleEntryExitLog.builder()
                        .vehicleId(vehicle.getId())
                        .registrationNumber(vehicle.getRegistrationNumber())
                        .parkingSpaceId(parkingSpaceId != null ? parkingSpaceId : 1L)
                        .userId(vehicle.getUserId())
                        .entryTime(exitTime.minusHours(1))
                        .hourlyRate(BigDecimal.valueOf(10.00))
                        .build());

        long durationMinutes = Math.max(1, Duration.between(activeLog.getEntryTime(), exitTime).toMinutes());
        BigDecimal rate = activeLog.getHourlyRate() != null ? activeLog.getHourlyRate() : BigDecimal.valueOf(10.00);
        long hours = (long) Math.ceil((double) durationMinutes / 60.0);
        BigDecimal totalFee = rate.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);

        activeLog.setExitTime(exitTime);
        activeLog.setDurationMinutes(durationMinutes);
        activeLog.setTotalFee(totalFee);
        activeLog.setStatus("COMPLETED");

        VehicleEntryExitLog updatedLog = logRepository.save(activeLog);

        vehicle.setParkingStatus(VehicleParkingStatus.OUT_OF_PARKING);
        vehicle.setCurrentParkingSpaceId(null);
        vehicleRepository.save(vehicle);

        try {
            userClient.logUserActivity(
                    vehicle.getUserId(),
                    "VEHICLE_EXIT",
                    "Vehicle " + vehicle.getRegistrationNumber() + " exited parking space " + parkingSpaceId +
                            ". Duration: " + durationMinutes + " min, Fee: $" + totalFee);
        } catch (Exception e) {
            log.warn("Could not log user activity: {}", e.getMessage());
        }

        return mapToLogResponse(updatedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleLogResponse> getLogsByVehicleId(Long vehicleId) {
        return logRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleLogResponse> getLogsByUserId(Long userId) {
        return logRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleLogResponse> getAllLogs() {
        return logRepository.findAll().stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .vehicleType(vehicle.getVehicleType())
                .userId(vehicle.getUserId())
                .parkingStatus(vehicle.getParkingStatus())
                .currentParkingSpaceId(vehicle.getCurrentParkingSpaceId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    private VehicleLogResponse mapToLogResponse(VehicleEntryExitLog logItem) {
        return VehicleLogResponse.builder()
                .id(logItem.getId())
                .vehicleId(logItem.getVehicleId())
                .registrationNumber(logItem.getRegistrationNumber())
                .parkingSpaceId(logItem.getParkingSpaceId())
                .userId(logItem.getUserId())
                .entryTime(logItem.getEntryTime())
                .exitTime(logItem.getExitTime())
                .durationMinutes(logItem.getDurationMinutes())
                .hourlyRate(logItem.getHourlyRate())
                .totalFee(logItem.getTotalFee())
                .status(logItem.getStatus())
                .createdAt(logItem.getCreatedAt())
                .build();
    }
}
