package com.spms.parkingspaceservice.dto;

import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotNull(message = "Status is required (AVAILABLE, RESERVED, OCCUPIED)")
    private ParkingSpaceStatus status;

    private Long userId;
    private Long vehicleId;
    private String reason;
}
