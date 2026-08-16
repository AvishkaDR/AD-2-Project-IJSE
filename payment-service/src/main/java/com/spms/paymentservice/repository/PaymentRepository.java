package com.spms.paymentservice.repository;

import com.spms.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<Payment> findByParkingSpaceIdOrderByCreatedAtDesc(Long parkingSpaceId);
}
