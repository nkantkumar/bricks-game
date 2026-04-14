package com.xyz.investment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PaymentFixtures {

    private PaymentFixtures() {
    }

    public static PaymentRequest validRequest() {
        return PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
    }

    public static PaymentRequest highValueRequest() {
        return validRequest().toBuilder()
                .amount(new BigDecimal("999999.00"))
                .build();
    }

    public static Payment pendingPayment() {
        return Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public static Payment capturedPayment() {
        return pendingPayment().toBuilder()
                .status(PaymentStatus.CAPTURED)
                .build();
    }
}
