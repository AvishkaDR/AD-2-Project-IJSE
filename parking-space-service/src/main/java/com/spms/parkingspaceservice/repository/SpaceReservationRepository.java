package com.spms.parkingspaceservice.repository;

import com.spms.parkingspaceservice.entity.ReservationStatus;
import com.spms.parkingspaceservice.entity.SpaceReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceReservationRepository extends JpaRepository<SpaceReservation, Long> {

    List<SpaceReservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SpaceReservation> findByParkingSpaceIdOrderByCreatedAtDesc(Long parkingSpaceId);

    Optional<SpaceReservation> findFirstByParkingSpaceIdAndStatusOrderByCreatedAtDesc(
            Long parkingSpaceId, ReservationStatus status);

    Optional<SpaceReservation> findFirstByParkingSpaceIdAndUserIdAndStatusOrderByCreatedAtDesc(
            Long parkingSpaceId, Long userId, ReservationStatus status);
}
