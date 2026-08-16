package com.spms.vehicleservice;

import com.spms.vehicleservice.client.ParkingSpaceClient;
import com.spms.vehicleservice.client.UserClient;
import com.spms.vehicleservice.dto.ParkingSpaceDto;
import com.spms.vehicleservice.dto.VehicleEntryRequest;
import com.spms.vehicleservice.dto.VehicleExitRequest;
import com.spms.vehicleservice.dto.VehicleLogResponse;
import com.spms.vehicleservice.dto.VehicleRegisterRequest;
import com.spms.vehicleservice.dto.VehicleResponse;
import com.spms.vehicleservice.entity.Vehicle;
import com.spms.vehicleservice.entity.VehicleEntryExitLog;
import com.spms.vehicleservice.entity.VehicleParkingStatus;
import com.spms.vehicleservice.entity.VehicleType;
import com.spms.vehicleservice.repository.VehicleEntryExitLogRepository;
import com.spms.vehicleservice.repository.VehicleRepository;
import com.spms.vehicleservice.service.VehicleServiceImpl;
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
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleEntryExitLogRepository logRepository;

    @Mock
    private ParkingSpaceClient parkingSpaceClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleVehicle = Vehicle.builder()
                .id(1L)
                .registrationNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .userId(1L)
                .parkingStatus(VehicleParkingStatus.OUT_OF_PARKING)
                .make("Toyota")
                .model("Corolla")
                .color("White")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testRegisterVehicle_Success() {
        VehicleRegisterRequest request = VehicleRegisterRequest.builder()
                .registrationNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .userId(1L)
                .make("Toyota")
                .model("Corolla")
                .color("White")
                .build();

        when(vehicleRepository.existsByRegistrationNumber("WP-CAB-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        VehicleResponse response = vehicleService.registerVehicle(request);

        assertNotNull(response);
        assertEquals("WP-CAB-1234", response.getRegistrationNumber());
        assertEquals(VehicleParkingStatus.OUT_OF_PARKING, response.getParkingStatus());
    }

    @Test
    void testSimulateEntry_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        ParkingSpaceDto spaceDto = ParkingSpaceDto.builder()
                .id(10L)
                .spaceNumber("A-101")
                .hourlyRate(BigDecimal.valueOf(20.00))
                .build();
        when(parkingSpaceClient.getParkingSpaceById(10L)).thenReturn(spaceDto);

        VehicleEntryExitLog sampleLog = VehicleEntryExitLog.builder()
                .id(1L)
                .vehicleId(1L)
                .registrationNumber("WP-CAB-1234")
                .parkingSpaceId(10L)
                .userId(1L)
                .entryTime(LocalDateTime.now())
                .hourlyRate(BigDecimal.valueOf(20.00))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        when(logRepository.save(any(VehicleEntryExitLog.class))).thenReturn(sampleLog);

        VehicleEntryRequest request = VehicleEntryRequest.builder()
                .vehicleId(1L)
                .parkingSpaceId(10L)
                .build();

        VehicleLogResponse response = vehicleService.simulateEntry(request);

        assertNotNull(response);
        assertEquals(1L, response.getVehicleId());
        assertEquals(10L, response.getParkingSpaceId());
        assertEquals("ACTIVE", response.getStatus());
    }
}
