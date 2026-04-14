package com.xyz.investment;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class Payment {
    private String paymentId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String idempotencyKey;
    private Instant createdAt;
}

 enum PaymentStatus {
    PENDING, APPROVED, REJECTED, CAPTURED, FAILED
}

@Data
@Builder
 class PaymentRequest {
    @NotNull private BigDecimal amount;
    @NotBlank private String currency;
    @NotBlank private String merchantId;
    @NotBlank private String idempotencyKey;
}

@Data
@Builder
 class PaymentResponse {
    private String paymentId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private Instant createdAt;
}