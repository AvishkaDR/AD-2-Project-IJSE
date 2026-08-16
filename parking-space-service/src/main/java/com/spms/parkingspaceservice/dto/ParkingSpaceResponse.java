package com.spms.parkingspaceservice.dto;

import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpaceResponse {

    private Long id;
    private String spaceNumber;
    private String location;
    private String city;
    private String zone;
    private Long ownerId;
    private ParkingSpaceStatus status;
    private BigDecimal hourlyRate;
    private String spaceType;
    private String sensorId;
    private LocalDateTime lastSensorUpdate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
