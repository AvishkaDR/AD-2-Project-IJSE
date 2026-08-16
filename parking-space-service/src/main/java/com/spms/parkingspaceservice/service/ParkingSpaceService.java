package com.spms.parkingspaceservice.service;

import com.spms.parkingspaceservice.dto.*;
import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;

import java.util.List;

public interface ParkingSpaceService {

    ParkingSpaceResponse createParkingSpace(ParkingSpaceCreateRequest request);

    ParkingSpaceResponse getParkingSpaceById(Long id);

    ParkingSpaceResponse getParkingSpaceByNumber(String spaceNumber);

    List<ParkingSpaceResponse> getAllParkingSpaces();

    List<ParkingSpaceResponse> searchParkingSpaces(String city, String zone, ParkingSpaceStatus status, Long ownerId);

    ParkingSpaceResponse updateParkingSpace(Long id, ParkingSpaceUpdateRequest request);

    void deleteParkingSpace(Long id);

    ReservationResponse reserveParkingSpace(Long id, ReserveSpaceRequest request);

    ParkingSpaceResponse releaseParkingSpace(Long id, ReleaseSpaceRequest request);

    ParkingSpaceResponse markOccupied(Long id, OccupySpaceRequest request);

    ParkingSpaceResponse markAvailable(Long id, VacateSpaceRequest request);

    ParkingSpaceResponse updateStatus(Long id, StatusUpdateRequest request);

    ParkingSpaceResponse processIoTSensorUpdate(IoTSensorUpdateRequest request);

    List<ReservationResponse> getReservationsBySpaceId(Long spaceId);

    List<ReservationResponse> getReservationsByUserId(Long userId);
}
