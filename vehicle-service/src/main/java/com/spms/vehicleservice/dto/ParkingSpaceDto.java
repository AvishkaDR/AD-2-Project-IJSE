package com.spms.vehicleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpaceDto {

    private Long id;
    private String spaceNumber;
    private String location;
    private String city;
    private String zone;
    private Long ownerId;
    private String status;
    private BigDecimal hourlyRate;
    private String spaceType;
}
