package com.spms.vehicleservice.repository;

import com.spms.vehicleservice.entity.VehicleEntryExitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleEntryExitLogRepository extends JpaRepository<VehicleEntryExitLog, Long> {

    List<VehicleEntryExitLog> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<VehicleEntryExitLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<VehicleEntryExitLog> findFirstByVehicleIdAndStatusOrderByCreatedAtDesc(
            Long vehicleId, String status);
}
