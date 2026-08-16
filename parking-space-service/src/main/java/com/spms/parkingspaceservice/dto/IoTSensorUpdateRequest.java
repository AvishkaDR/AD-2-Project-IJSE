package com.spms.parkingspaceservice.dto;

import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IoTSensorUpdateRequest {

    @NotBlank(message = "Sensor ID is required")
    private String sensorId;

    private Long parkingSpaceId;

    @NotNull(message = "Status is required (AVAILABLE, OCCUPIED)")
    private ParkingSpaceStatus status;

    private String remarks;
}
