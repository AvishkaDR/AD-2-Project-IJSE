package com.spms.vehicleservice.dto;

import com.spms.vehicleservice.entity.VehicleParkingStatus;
import com.spms.vehicleservice.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private Long id;
    private String registrationNumber;
    private VehicleType vehicleType;
    private Long userId;
    private VehicleParkingStatus parkingStatus;
    private Long currentParkingSpaceId;
    private String make;
    private String model;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
