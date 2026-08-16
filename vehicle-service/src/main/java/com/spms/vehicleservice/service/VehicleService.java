package com.spms.vehicleservice.service;

import com.spms.vehicleservice.dto.*;

import java.util.List;

public interface VehicleService {

    VehicleResponse registerVehicle(VehicleRegisterRequest request);

    VehicleResponse getVehicleById(Long id);

    VehicleResponse getVehicleByRegistrationNumber(String registrationNumber);

    List<VehicleResponse> getAllVehicles();

    List<VehicleResponse> getVehiclesByUserId(Long userId);

    VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request);

    void deleteVehicle(Long id);

    VehicleLogResponse simulateEntry(VehicleEntryRequest request);

    VehicleLogResponse simulateExit(VehicleExitRequest request);

    List<VehicleLogResponse> getLogsByVehicleId(Long vehicleId);

    List<VehicleLogResponse> getLogsByUserId(Long userId);

    List<VehicleLogResponse> getAllLogs();
}
