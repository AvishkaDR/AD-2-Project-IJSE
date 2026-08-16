package com.spms.vehicleservice.dto;

import com.spms.vehicleservice.entity.VehicleType;
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
public class VehicleRegisterRequest {

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotNull(message = "Vehicle type is required (CAR, MOTORCYCLE, SUV, TRUCK, EV)")
    private VehicleType vehicleType;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String make;

    private String model;

    private String color;
}
