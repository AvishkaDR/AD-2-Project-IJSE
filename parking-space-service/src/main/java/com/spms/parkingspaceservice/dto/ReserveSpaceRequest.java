package com.spms.parkingspaceservice.dto;

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
public class ReserveSpaceRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long vehicleId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
