package com.spms.parkingspaceservice.service;

import com.spms.parkingspaceservice.dto.*;
import com.spms.parkingspaceservice.entity.ParkingSpace;
import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;
import com.spms.parkingspaceservice.entity.ReservationStatus;
import com.spms.parkingspaceservice.entity.SpaceReservation;
import com.spms.parkingspaceservice.exception.DuplicateSpaceException;
import com.spms.parkingspaceservice.exception.InvalidStateTransitionException;
import com.spms.parkingspaceservice.exception.ResourceNotFoundException;
import com.spms.parkingspaceservice.exception.SpaceUnavailableException;
import com.spms.parkingspaceservice.repository.ParkingSpaceRepository;
import com.spms.parkingspaceservice.repository.SpaceReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingSpaceServiceImpl implements ParkingSpaceService {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final SpaceReservationRepository reservationRepository;

    @Override
    @Transactional
    public ParkingSpaceResponse createParkingSpace(ParkingSpaceCreateRequest request) {
        log.info("Creating parking space: {}", request.getSpaceNumber());
        if (parkingSpaceRepository.existsBySpaceNumber(request.getSpaceNumber())) {
            throw new DuplicateSpaceException("Parking space with number " + request.getSpaceNumber() + " already exists");
        }

        ParkingSpace space = ParkingSpace.builder()
                .spaceNumber(request.getSpaceNumber())
                .location(request.getLocation())
                .city(request.getCity())
                .zone(request.getZone())
                .ownerId(request.getOwnerId())
                .status(ParkingSpaceStatus.AVAILABLE)
                .hourlyRate(request.getHourlyRate())
                .spaceType(request.getSpaceType() != null ? request.getSpaceType() : "STANDARD")
                .sensorId(request.getSensorId())
                .build();

        ParkingSpace saved = parkingSpaceRepository.save(space);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingSpaceResponse getParkingSpaceById(Long id) {
        ParkingSpace space = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with ID: " + id));
        return mapToResponse(space);
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingSpaceResponse getParkingSpaceByNumber(String spaceNumber) {
        ParkingSpace space = parkingSpaceRepository.findBySpaceNumber(spaceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with number: " + spaceNumber));
        return mapToResponse(space);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpaceResponse> getAllParkingSpaces() {
        return parkingSpaceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpaceResponse> searchParkingSpaces(String city, String zone, ParkingSpaceStatus status, Long ownerId) {
        return parkingSpaceRepository.searchSpaces(city, zone, status, ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParkingSpaceResponse updateParkingSpace(Long id, ParkingSpaceUpdateRequest request) {
        log.info("Updating parking space with ID: {}", id);
        ParkingSpace space = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with ID: " + id));

        if (request.getLocation() != null) space.setLocation(request.getLocation());
        if (request.getCity() != null) space.setCity(request.getCity());
        if (request.getZone() != null) space.setZone(request.getZone());
        if (request.getHourlyRate() != null) space.setHourlyRate(request.getHourlyRate());
        if (request.getSpaceType() != null) space.setSpaceType(request.getSpaceType());
        if (request.getStatus() != null) space.setStatus(request.getStatus());
        if (request.getSensorId() != null) space.setSensorId(request.getSensorId());

        ParkingSpace updated = parkingSpaceRepository.save(space);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteParkingSpace(Long id) {
        log.info("Deleting parking space with ID: {}", id);
        if (!parkingSpaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parking space not found with ID: " + id);
        }
        parkingSpaceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ReservationResponse reserveParkingSpace(Long id, ReserveSpaceRequest request) {
        log.info("Reserving parking space ID: {} for user: {}", id, request.getUserId());
        ParkingSpace space = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with ID: " + id));

        if (space.getStatus() != ParkingSpaceStatus.AVAILABLE) {
            throw new SpaceUnavailableException(
                    "Parking space " + space.getSpaceNumber() + " cannot be reserved because it is currently " + space.getStatus());
        }

        space.setStatus(ParkingSpaceStatus.RESERVED);
        parkingSpaceRepository.save(space);

        SpaceReservation reservation = SpaceReservation.builder()
                .parkingSpaceId(space.getId())
                .userId(request.getUserId())
                .vehicleId(request.getVehicleId())
                .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
                .endTime(request.getEndTime())
                .status(ReservationStatus.ACTIVE)
                .build();

        SpaceReservation savedReservation = reservationRepository.save(reservation);
        return mapToReservationResponse(savedReservation);
    }

    @Override
    @Transactional
    public ParkingSpaceResponse releaseParkingSpace(Long id, ReleaseSpaceRequest request) {
        log.info("Releasing parking space ID: {}", id);
        ParkingSpace space = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with ID: " + id));

        if (space.getStatus() != ParkingSpaceStatus.RESERVED) {
            throw new InvalidStateTransitionException(
                    "Cannot release space " + space.getSpaceNumber() + " because it is not in RESERVED state (current: " + space.getStatus() + ")");
        }

        space.setStatus(ParkingSpaceStatus.AVAILABLE);
        ParkingSpace updated = parkingSpaceRepository.save(space);

        reservationRepository.findFirstByParkingSpaceIdAndStatusOrderByCreatedAtDesc(id, ReservationStatus.ACTIVE)
                .ifPresent(res -> {
                    res.setStatus(ReservationStatus.CANCELLED);
                    res.setEndTime(LocalDateTime.now());
                    reservationRepository.save(res);
                });

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ParkingSpaceResponse markOccupied(Long id, OccupySpaceRequest request) {
        log.info("Marking parking space ID: {} as OCCUPIED", id);
        ParkingSpace space = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with ID: " + id));

        if (space.getStatus() == ParkingSpaceStatus.OCCUPIED) {
            throw new InvalidStateTransitionException("Parking space " + space.getSpaceNumber() + " is already OCCUPIED");
        }

        space.setStatus(ParkingSpaceStatus.OCCUPIED);
        space.setLastSensorUpdate(LocalDateTime.now());
        ParkingSpace updated = parkingSpaceRepository.save(space);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ParkingSpaceResponse markAvailable(Long id, VacateSpaceRequest request) {
        log.info("Marking parking space ID: {} as AVAILABLE", id);
        ParkingSpace space = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with ID: " + id));

        if (space.getStatus() == ParkingSpaceStatus.AVAILABLE) {
            throw new InvalidStateTransitionException("Parking space " + space.getSpaceNumber() + " is already AVAILABLE");
        }

        space.setStatus(ParkingSpaceStatus.AVAILABLE);
        space.setLastSensorUpdate(LocalDateTime.now());
        ParkingSpace updated = parkingSpaceRepository.save(space);

        reservationRepository.findFirstByParkingSpaceIdAndStatusOrderByCreatedAtDesc(id, ReservationStatus.ACTIVE)
                .ifPresent(res -> {
                    res.setStatus(ReservationStatus.COMPLETED);
                    res.setEndTime(LocalDateTime.now());
                    reservationRepository.save(res);
                });

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ParkingSpaceResponse updateStatus(Long id, StatusUpdateRequest request) {
        log.info("Manual status update for space ID: {} to {}", id, request.getStatus());
        ParkingSpace space = parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with ID: " + id));

        ParkingSpaceStatus currentStatus = space.getStatus();
        ParkingSpaceStatus targetStatus = request.getStatus();

        if (currentStatus == targetStatus) {
            return mapToResponse(space);
        }

        // Validate allowed state transitions
        validateStateTransition(space.getSpaceNumber(), currentStatus, targetStatus);

        space.setStatus(targetStatus);
        space.setLastSensorUpdate(LocalDateTime.now());
        ParkingSpace updated = parkingSpaceRepository.save(space);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ParkingSpaceResponse processIoTSensorUpdate(IoTSensorUpdateRequest request) {
        log.info("Processing IoT sensor update for sensor: {}, status: {}", request.getSensorId(), request.getStatus());
        ParkingSpace space;
        if (request.getParkingSpaceId() != null) {
            space = parkingSpaceRepository.findById(request.getParkingSpaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with ID: " + request.getParkingSpaceId()));
        } else {
            space = parkingSpaceRepository.findBySensorId(request.getSensorId())
                    .orElseThrow(() -> new ResourceNotFoundException("No parking space associated with sensor ID: " + request.getSensorId()));
        }

        space.setStatus(request.getStatus());
        space.setLastSensorUpdate(LocalDateTime.now());
        ParkingSpace updated = parkingSpaceRepository.save(space);

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsBySpaceId(Long spaceId) {
        return reservationRepository.findByParkingSpaceIdOrderByCreatedAtDesc(spaceId).stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
    }

    private void validateStateTransition(String spaceNumber, ParkingSpaceStatus from, ParkingSpaceStatus to) {
        // Valid transitions:
        // AVAILABLE -> RESERVED
        // AVAILABLE -> OCCUPIED
        // RESERVED -> OCCUPIED
        // RESERVED -> AVAILABLE
        // OCCUPIED -> AVAILABLE
        boolean valid = switch (from) {
            case AVAILABLE -> to == ParkingSpaceStatus.RESERVED || to == ParkingSpaceStatus.OCCUPIED;
            case RESERVED -> to == ParkingSpaceStatus.OCCUPIED || to == ParkingSpaceStatus.AVAILABLE;
            case OCCUPIED -> to == ParkingSpaceStatus.AVAILABLE;
        };

        if (!valid) {
            throw new InvalidStateTransitionException(
                    "Invalid state transition for space " + spaceNumber + " from " + from + " to " + to);
        }
    }

    private ParkingSpaceResponse mapToResponse(ParkingSpace space) {
        return ParkingSpaceResponse.builder()
                .id(space.getId())
                .spaceNumber(space.getSpaceNumber())
                .location(space.getLocation())
                .city(space.getCity())
                .zone(space.getZone())
                .ownerId(space.getOwnerId())
                .status(space.getStatus())
                .hourlyRate(space.getHourlyRate())
                .spaceType(space.getSpaceType())
                .sensorId(space.getSensorId())
                .lastSensorUpdate(space.getLastSensorUpdate())
                .createdAt(space.getCreatedAt())
                .updatedAt(space.getUpdatedAt())
                .build();
    }

    private ReservationResponse mapToReservationResponse(SpaceReservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .parkingSpaceId(reservation.getParkingSpaceId())
                .userId(reservation.getUserId())
                .vehicleId(reservation.getVehicleId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
