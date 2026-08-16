package com.spms.vehicleservice.dto;

import com.spms.vehicleservice.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUpdateRequest {

    private VehicleType vehicleType;
    private String make;
    private String model;
    private String color;
}
