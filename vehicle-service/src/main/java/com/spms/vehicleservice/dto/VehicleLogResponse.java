package com.spms.vehicleservice.dto;

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
public class VehicleLogResponse {

    private Long id;
    private Long vehicleId;
    private String registrationNumber;
    private Long parkingSpaceId;
    private Long userId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long durationMinutes;
    private BigDecimal hourlyRate;
    private BigDecimal totalFee;
    private String status;
    private LocalDateTime createdAt;
}
