package com.xyz.investment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull private BigDecimal amount;
    @NotBlank private String currency;
    @NotBlank private String merchantId;
    @NotBlank private String idempotencyKey;
}
