package com.spms.paymentservice.repository;

import com.spms.paymentservice.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByReceiptId(String receiptId);

    Optional<Receipt> findByTransactionId(String transactionId);

    List<Receipt> findByUserIdOrderByTimestampDesc(Long userId);

    List<Receipt> findByVehicleIdOrderByTimestampDesc(Long vehicleId);
}
