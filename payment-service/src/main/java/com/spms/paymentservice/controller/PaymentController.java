package com.spms.paymentservice.controller;

import com.spms.paymentservice.dto.PaymentProcessRequest;
import com.spms.paymentservice.dto.PaymentResponse;
import com.spms.paymentservice.dto.ReceiptResponse;
import com.spms.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentProcessRequest request) {
        log.info("Received payment process request: amount={}, userId={}", request.getAmount(), request.getUserId());
        PaymentResponse response = paymentService.processPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.info("Received request to get payment by ID: {}", id);
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentByTransactionId(@PathVariable String transactionId) {
        log.info("Received request to get payment by transaction ID: {}", transactionId);
        PaymentResponse response = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            log.info("Received request to get payments for user ID: {}", userId);
            List<PaymentResponse> list = paymentService.getPaymentsByUserId(userId);
            return ResponseEntity.ok(list);
        }
        log.info("Received request to list all payments");
        List<PaymentResponse> list = paymentService.getAllPayments();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        log.info("Received request to get payments for user ID: {}", userId);
        List<PaymentResponse> list = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/receipts/{id}")
    public ResponseEntity<ReceiptResponse> getReceiptById(@PathVariable Long id) {
        log.info("Received request to get receipt by ID: {}", id);
        ReceiptResponse response = paymentService.getReceiptById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receipts/receipt-id/{receiptId}")
    public ResponseEntity<ReceiptResponse> getReceiptByReceiptId(@PathVariable String receiptId) {
        log.info("Received request to get receipt by receipt ID: {}", receiptId);
        ReceiptResponse response = paymentService.getReceiptByReceiptId(receiptId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receipts/transaction/{transactionId}")
    public ResponseEntity<ReceiptResponse> getReceiptByTransactionId(@PathVariable String transactionId) {
        log.info("Received request to get receipt by transaction ID: {}", transactionId);
        ReceiptResponse response = paymentService.getReceiptByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receipts/user/{userId}")
    public ResponseEntity<List<ReceiptResponse>> getReceiptsByUserId(@PathVariable Long userId) {
        log.info("Received request to get receipts for user ID: {}", userId);
        List<ReceiptResponse> list = paymentService.getReceiptsByUserId(userId);
        return ResponseEntity.ok(list);
    }
}
