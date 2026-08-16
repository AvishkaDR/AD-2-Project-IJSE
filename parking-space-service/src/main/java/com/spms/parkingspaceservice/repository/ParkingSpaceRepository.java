package com.spms.parkingspaceservice.repository;

import com.spms.parkingspaceservice.entity.ParkingSpace;
import com.spms.parkingspaceservice.entity.ParkingSpaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long>, JpaSpecificationExecutor<ParkingSpace> {

    Optional<ParkingSpace> findBySpaceNumber(String spaceNumber);

    boolean existsBySpaceNumber(String spaceNumber);

    Optional<ParkingSpace> findBySensorId(String sensorId);

    List<ParkingSpace> findByStatus(ParkingSpaceStatus status);

    List<ParkingSpace> findByOwnerId(Long ownerId);

    List<ParkingSpace> findByCityIgnoreCase(String city);

    List<ParkingSpace> findByZoneIgnoreCase(String zone);

    @Query("SELECT p FROM ParkingSpace p WHERE " +
            "(:city IS NULL OR LOWER(p.city) = LOWER(:city)) AND " +
            "(:zone IS NULL OR LOWER(p.zone) = LOWER(:zone)) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:ownerId IS NULL OR p.ownerId = :ownerId)")
    List<ParkingSpace> searchSpaces(
            @Param("city") String city,
            @Param("zone") String zone,
            @Param("status") ParkingSpaceStatus status,
            @Param("ownerId") Long ownerId);
}
