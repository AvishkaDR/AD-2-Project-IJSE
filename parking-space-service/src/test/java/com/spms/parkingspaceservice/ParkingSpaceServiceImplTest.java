package com.spms.parkingspaceservice;

import com.spms.parkingspaceservice.dto.ParkingSpaceCreateRequest;
import com.spms.parkingspaceservice.dto.ParkingSpaceResponse;
import com.spms.parkingspaceservice.dto.ReserveSpaceRequest;
import com.spms.parkingspaceservice.dto.ReservationResponse;
import com.spms.parkingspaceservice.entity.ParkingSpace;
import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;
import com.spms.parkingspaceservice.entity.ReservationStatus;
import com.spms.parkingspaceservice.entity.SpaceReservation;
import com.spms.parkingspaceservice.exception.InvalidStateTransitionException;
import com.spms.parkingspaceservice.repository.ParkingSpaceRepository;
import com.spms.parkingspaceservice.repository.SpaceReservationRepository;
import com.spms.parkingspaceservice.service.ParkingSpaceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSpaceServiceImplTest {

    @Mock
    private ParkingSpaceRepository parkingSpaceRepository;

    @Mock
    private SpaceReservationRepository reservationRepository;

    @InjectMocks
    private ParkingSpaceServiceImpl parkingSpaceService;

    private ParkingSpace sampleSpace;

    @BeforeEach
    void setUp() {
        sampleSpace = ParkingSpace.builder()
                .id(1L)
                .spaceNumber("A-101")
                .location("Downtown Mall")
                .city("Colombo")
                .zone("Zone-A")
                .ownerId(10L)
                .status(ParkingSpaceStatus.AVAILABLE)
                .hourlyRate(BigDecimal.valueOf(15.00))
                .spaceType("STANDARD")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateParkingSpace_Success() {
        ParkingSpaceCreateRequest request = ParkingSpaceCreateRequest.builder()
                .spaceNumber("A-101")
                .location("Downtown Mall")
                .city("Colombo")
                .zone("Zone-A")
                .ownerId(10L)
                .hourlyRate(BigDecimal.valueOf(15.00))
                .build();

        when(parkingSpaceRepository.existsBySpaceNumber("A-101")).thenReturn(false);
        when(parkingSpaceRepository.save(any(ParkingSpace.class))).thenReturn(sampleSpace);

        ParkingSpaceResponse response = parkingSpaceService.createParkingSpace(request);

        assertNotNull(response);
        assertEquals("A-101", response.getSpaceNumber());
        assertEquals(ParkingSpaceStatus.AVAILABLE, response.getStatus());
    }

    @Test
    void testReserveParkingSpace_Success() {
        when(parkingSpaceRepository.findById(1L)).thenReturn(Optional.of(sampleSpace));
        when(parkingSpaceRepository.save(any(ParkingSpace.class))).thenReturn(sampleSpace);

        SpaceReservation sampleReservation = SpaceReservation.builder()
                .id(1L)
                .parkingSpaceId(1L)
                .userId(5L)
                .startTime(LocalDateTime.now())
                .status(ReservationStatus.ACTIVE)
                .build();
        when(reservationRepository.save(any(SpaceReservation.class))).thenReturn(sampleReservation);

        ReserveSpaceRequest request = ReserveSpaceRequest.builder()
                .userId(5L)
                .build();

        ReservationResponse response = parkingSpaceService.reserveParkingSpace(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getParkingSpaceId());
        assertEquals(5L, response.getUserId());
    }
}
