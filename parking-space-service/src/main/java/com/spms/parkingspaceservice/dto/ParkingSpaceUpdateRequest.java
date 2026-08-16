package com.spms.parkingspaceservice.dto;

import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpaceUpdateRequest {

    private String location;
    private String city;
    private String zone;
    private BigDecimal hourlyRate;
    private String spaceType;
    private ParkingSpaceStatus status;
    private String sensorId;
}
