package com.xyz.investment;

import java.math.BigDecimal;

public record PaymentEvent(
        String paymentId,
        String merchantId,
        BigDecimal amount,
        String currency
) {
    public static PaymentEvent of(Payment payment) {
        return new PaymentEvent(
                payment.getPaymentId(),
                payment.getMerchantId(),
                payment.getAmount(),
                payment.getCurrency()
        );
    }
}
