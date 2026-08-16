package com.spms.paymentservice.service;

import com.spms.paymentservice.client.UserClient;
import com.spms.paymentservice.dto.MockCardDetails;
import com.spms.paymentservice.dto.PaymentProcessRequest;
import com.spms.paymentservice.dto.PaymentResponse;
import com.spms.paymentservice.dto.ReceiptResponse;
import com.spms.paymentservice.entity.Payment;
import com.spms.paymentservice.entity.PaymentMethod;
import com.spms.paymentservice.entity.PaymentStatus;
import com.spms.paymentservice.entity.Receipt;
import com.spms.paymentservice.exception.InvalidPaymentException;
import com.spms.paymentservice.exception.PaymentFailedException;
import com.spms.paymentservice.exception.ResourceNotFoundException;
import com.spms.paymentservice.repository.PaymentRepository;
import com.spms.paymentservice.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentProcessRequest request) {
        log.info("Processing payment of amount {} for userId: {}", request.getAmount(), request.getUserId());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Payment amount must be greater than zero");
        }

        // Validate mock card data if card payment
        if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD || request.getPaymentMethod() == PaymentMethod.DEBIT_CARD) {
            validateCardDetails(request.getCardDetails());
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Simulate decline rule for testing: card ending with "0000" fails
        boolean isDeclined = false;
        String failureReason = null;
        if (request.getCardDetails() != null && request.getCardDetails().getCardNumber() != null) {
            if (request.getCardDetails().getCardNumber().endsWith("0000")) {
                isDeclined = true;
                failureReason = "Card declined by mock issuing bank (Insufficient funds)";
            }
        }

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .userId(request.getUserId())
                .vehicleId(request.getVehicleId())
                .parkingSpaceId(request.getParkingSpaceId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(isDeclined ? PaymentStatus.FAILED : PaymentStatus.SUCCESS)
                .failureReason(failureReason)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (isDeclined) {
            log.warn("Payment failed for transaction: {}", transactionId);
            try {
                userClient.logUserActivity(
                        request.getUserId(),
                        "PAYMENT_FAILED",
                        "Payment of $" + request.getAmount() + " failed: " + failureReason);
            } catch (Exception e) {
                log.warn("Could not log user activity: {}", e.getMessage());
            }
            throw new PaymentFailedException("Payment processing failed: " + failureReason);
        }

        // Generate digital receipt
        String receiptId = "RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String digitalSignature = generateDigitalSignature(transactionId, receiptId, request.getAmount());

        Receipt receipt = Receipt.builder()
                .receiptId(receiptId)
                .transactionId(transactionId)
                .userId(request.getUserId())
                .vehicleId(request.getVehicleId())
                .parkingSpaceId(request.getParkingSpaceId())
                .amount(request.getAmount())
                .status(PaymentStatus.SUCCESS)
                .digitalSignature(digitalSignature)
                .build();

        Receipt savedReceipt = receiptRepository.save(receipt);

        try {
            userClient.logUserActivity(
                    request.getUserId(),
                    "PAYMENT_SUCCESS",
                    "Payment of $" + request.getAmount() + " processed successfully. Receipt: " + receiptId);
        } catch (Exception e) {
            log.warn("Could not log user activity: {}", e.getMessage());
        }

        ReceiptResponse receiptResponse = mapToReceiptResponse(savedReceipt);
        return mapToPaymentResponse(savedPayment, receiptResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));

        ReceiptResponse receiptResponse = receiptRepository.findByTransactionId(payment.getTransactionId())
                .map(this::mapToReceiptResponse)
                .orElse(null);

        return mapToPaymentResponse(payment, receiptResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));

        ReceiptResponse receiptResponse = receiptRepository.findByTransactionId(transactionId)
                .map(this::mapToReceiptResponse)
                .orElse(null);

        return mapToPaymentResponse(payment, receiptResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(payment -> {
                    ReceiptResponse receiptResponse = receiptRepository.findByTransactionId(payment.getTransactionId())
                            .map(this::mapToReceiptResponse)
                            .orElse(null);
                    return mapToPaymentResponse(payment, receiptResponse);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(payment -> {
                    ReceiptResponse receiptResponse = receiptRepository.findByTransactionId(payment.getTransactionId())
                            .map(this::mapToReceiptResponse)
                            .orElse(null);
                    return mapToPaymentResponse(payment, receiptResponse);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptById(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with ID: " + id));
        return mapToReceiptResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByReceiptId(String receiptId) {
        Receipt receipt = receiptRepository.findByReceiptId(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with receipt ID: " + receiptId));
        return mapToReceiptResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByTransactionId(String transactionId) {
        Receipt receipt = receiptRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with transaction ID: " + transactionId));
        return mapToReceiptResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByUserId(Long userId) {
        return receiptRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(this::mapToReceiptResponse)
                .collect(Collectors.toList());
    }

    private void validateCardDetails(MockCardDetails cardDetails) {
        if (cardDetails == null) {
            throw new InvalidPaymentException("Card details must be provided for card payment");
        }
        if (cardDetails.getCardNumber() == null || cardDetails.getCardNumber().replaceAll("\\s+", "").length() < 13) {
            throw new InvalidPaymentException("Invalid card number format");
        }
        if (cardDetails.getCvv() == null || cardDetails.getCvv().length() < 3) {
            throw new InvalidPaymentException("Invalid CVV format");
        }
        if (cardDetails.getExpiryDate() == null || !cardDetails.getExpiryDate().matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            throw new InvalidPaymentException("Invalid expiry date format. Expected MM/YY");
        }
    }

    private String generateDigitalSignature(String transactionId, String receiptId, BigDecimal amount) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = "SPMS:" + transactionId + ":" + receiptId + ":" + amount + ":" + LocalDateTime.now();
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return "SIG-" + HexFormat.of().formatHex(hash).substring(0, 24).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "SIG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
        }
    }

    private PaymentResponse mapToPaymentResponse(Payment payment, ReceiptResponse receipt) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .userId(payment.getUserId())
                .vehicleId(payment.getVehicleId())
                .parkingSpaceId(payment.getParkingSpaceId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .failureReason(payment.getFailureReason())
                .receipt(receipt)
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private ReceiptResponse mapToReceiptResponse(Receipt receipt) {
        return ReceiptResponse.builder()
                .id(receipt.getId())
                .receiptId(receipt.getReceiptId())
                .transactionId(receipt.getTransactionId())
                .userId(receipt.getUserId())
                .vehicleId(receipt.getVehicleId())
                .parkingSpaceId(receipt.getParkingSpaceId())
                .amount(receipt.getAmount())
                .status(receipt.getStatus())
                .timestamp(receipt.getTimestamp())
                .digitalSignature(receipt.getDigitalSignature())
                .build();
    }
}
