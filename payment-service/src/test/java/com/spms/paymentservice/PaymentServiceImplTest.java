package com.spms.paymentservice;

import com.spms.paymentservice.client.UserClient;
import com.spms.paymentservice.dto.MockCardDetails;
import com.spms.paymentservice.dto.PaymentProcessRequest;
import com.spms.paymentservice.dto.PaymentResponse;
import com.spms.paymentservice.entity.Payment;
import com.spms.paymentservice.entity.PaymentMethod;
import com.spms.paymentservice.entity.PaymentStatus;
import com.spms.paymentservice.entity.Receipt;
import com.spms.paymentservice.exception.PaymentFailedException;
import com.spms.paymentservice.repository.PaymentRepository;
import com.spms.paymentservice.repository.ReceiptRepository;
import com.spms.paymentservice.service.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentProcessRequest validRequest;

    @BeforeEach
    void setUp() {
        MockCardDetails card = MockCardDetails.builder()
                .cardholderName("John Doe")
                .cardNumber("4111222233334444")
                .expiryDate("12/28")
                .cvv("123")
                .build();

        validRequest = PaymentProcessRequest.builder()
                .userId(1L)
                .vehicleId(2L)
                .parkingSpaceId(3L)
                .amount(BigDecimal.valueOf(25.50))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .cardDetails(card)
                .build();
    }

    @Test
    void testProcessPayment_Success() {
        Payment savedPayment = Payment.builder()
                .id(1L)
                .transactionId("TXN-12345678")
                .userId(1L)
                .vehicleId(2L)
                .parkingSpaceId(3L)
                .amount(BigDecimal.valueOf(25.50))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        Receipt savedReceipt = Receipt.builder()
                .id(1L)
                .receiptId("RCP-12345678")
                .transactionId("TXN-12345678")
                .userId(1L)
                .vehicleId(2L)
                .parkingSpaceId(3L)
                .amount(BigDecimal.valueOf(25.50))
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .digitalSignature("SIG-TEST123456")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(receiptRepository.save(any(Receipt.class))).thenReturn(savedReceipt);

        PaymentResponse response = paymentService.processPayment(validRequest);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertNotNull(response.getReceipt());
        assertEquals("RCP-12345678", response.getReceipt().getReceiptId());
    }

    @Test
    void testProcessPayment_DeclinedCard() {
        validRequest.getCardDetails().setCardNumber("4111222233330000");

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPayment(validRequest);
        });
    }
}
