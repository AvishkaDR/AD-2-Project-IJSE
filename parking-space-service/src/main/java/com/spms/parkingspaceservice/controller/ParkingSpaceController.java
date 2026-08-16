package com.spms.parkingspaceservice.controller;

import com.spms.parkingspaceservice.dto.*;
import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;
import com.spms.parkingspaceservice.service.ParkingSpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-spaces")
@RequiredArgsConstructor
@Slf4j
public class ParkingSpaceController {

    private final ParkingSpaceService parkingSpaceService;

    @PostMapping
    public ResponseEntity<ParkingSpaceResponse> createParkingSpace(
            @Valid @RequestBody ParkingSpaceCreateRequest request) {
        log.info("Received request to create parking space: {}", request.getSpaceNumber());
        ParkingSpaceResponse response = parkingSpaceService.createParkingSpace(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSpaceResponse> getParkingSpaceById(@PathVariable Long id) {
        log.info("Received request to get parking space by ID: {}", id);
        ParkingSpaceResponse response = parkingSpaceService.getParkingSpaceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/space-number/{spaceNumber}")
    public ResponseEntity<ParkingSpaceResponse> getParkingSpaceByNumber(@PathVariable String spaceNumber) {
        log.info("Received request to get parking space by space number: {}", spaceNumber);
        ParkingSpaceResponse response = parkingSpaceService.getParkingSpaceByNumber(spaceNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ParkingSpaceResponse>> getParkingSpaces(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) ParkingSpaceStatus status,
            @RequestParam(required = false) Long ownerId) {
        log.info("Received request to list/filter parking spaces (city: {}, zone: {}, status: {}, ownerId: {})",
                city, zone, status, ownerId);
        if (city != null || zone != null || status != null || ownerId != null) {
            List<ParkingSpaceResponse> filtered = parkingSpaceService.searchParkingSpaces(city, zone, status, ownerId);
            return ResponseEntity.ok(filtered);
        }
        List<ParkingSpaceResponse> all = parkingSpaceService.getAllParkingSpaces();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ParkingSpaceResponse>> searchParkingSpaces(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) ParkingSpaceStatus status,
            @RequestParam(required = false) Long ownerId) {
        List<ParkingSpaceResponse> result = parkingSpaceService.searchParkingSpaces(city, zone, status, ownerId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingSpaceResponse> updateParkingSpace(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSpaceUpdateRequest request) {
        log.info("Received request to update parking space ID: {}", id);
        ParkingSpaceResponse response = parkingSpaceService.updateParkingSpace(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParkingSpace(@PathVariable Long id) {
        log.info("Received request to delete parking space ID: {}", id);
        parkingSpaceService.deleteParkingSpace(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<ReservationResponse> reserveSpace(
            @PathVariable Long id,
            @Valid @RequestBody ReserveSpaceRequest request) {
        log.info("Received request to reserve space ID: {}", id);
        ReservationResponse response = parkingSpaceService.reserveParkingSpace(id, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<ParkingSpaceResponse> releaseSpace(
            @PathVariable Long id,
            @RequestBody(required = false) ReleaseSpaceRequest request) {
        log.info("Received request to release space ID: {}", id);
        if (request == null) {
            request = new ReleaseSpaceRequest();
        }
        ParkingSpaceResponse response = parkingSpaceService.releaseParkingSpace(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/occupy")
    public ResponseEntity<ParkingSpaceResponse> occupySpace(
            @PathVariable Long id,
            @RequestBody(required = false) OccupySpaceRequest request) {
        log.info("Received request to mark space ID: {} as OCCUPIED", id);
        if (request == null) {
            request = new OccupySpaceRequest();
        }
        ParkingSpaceResponse response = parkingSpaceService.markOccupied(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/vacate")
    public ResponseEntity<ParkingSpaceResponse> vacateSpace(
            @PathVariable Long id,
            @RequestBody(required = false) VacateSpaceRequest request) {
        log.info("Received request to mark space ID: {} as AVAILABLE (vacate)", id);
        if (request == null) {
            request = new VacateSpaceRequest();
        }
        ParkingSpaceResponse response = parkingSpaceService.markAvailable(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ParkingSpaceResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        log.info("Received manual status update request for space ID: {}", id);
        ParkingSpaceResponse response = parkingSpaceService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/iot-sensor-update")
    public ResponseEntity<ParkingSpaceResponse> processIoTSensorUpdate(
            @Valid @RequestBody IoTSensorUpdateRequest request) {
        log.info("Received simulated IoT sensor update for sensor ID: {}", request.getSensorId());
        ParkingSpaceResponse response = parkingSpaceService.processIoTSensorUpdate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsBySpaceId(@PathVariable Long id) {
        List<ReservationResponse> reservations = parkingSpaceService.getReservationsBySpaceId(id);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/reservations/user/{userId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByUserId(@PathVariable Long userId) {
        List<ReservationResponse> reservations = parkingSpaceService.getReservationsByUserId(userId);
        return ResponseEntity.ok(reservations);
    }
}
