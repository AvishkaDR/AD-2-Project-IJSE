package com.spms.paymentservice.service;

import com.spms.paymentservice.dto.PaymentProcessRequest;
import com.spms.paymentservice.dto.PaymentResponse;
import com.spms.paymentservice.dto.ReceiptResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(PaymentProcessRequest request);

    PaymentResponse getPaymentById(Long id);

    PaymentResponse getPaymentByTransactionId(String transactionId);

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getPaymentsByUserId(Long userId);

    ReceiptResponse getReceiptById(Long id);

    ReceiptResponse getReceiptByReceiptId(String receiptId);

    ReceiptResponse getReceiptByTransactionId(String transactionId);

    List<ReceiptResponse> getReceiptsByUserId(Long userId);
}
