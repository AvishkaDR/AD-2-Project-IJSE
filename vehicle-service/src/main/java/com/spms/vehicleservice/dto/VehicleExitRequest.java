package com.spms.vehicleservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleExitRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    private Long parkingSpaceId;

    private LocalDateTime exitTime;
}
