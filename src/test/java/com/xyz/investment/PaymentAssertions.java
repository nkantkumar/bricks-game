package com.xyz.investment;

import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentAssertions extends AbstractAssert<PaymentAssertions, PaymentResponse> {

    public PaymentAssertions(PaymentResponse actual) {
        super(actual, PaymentAssertions.class);
    }

    public static PaymentAssertions assertThatPayment(PaymentResponse p) {
        return new PaymentAssertions(p);
    }

    public PaymentAssertions isPending() {
        assertThat(actual.getStatus()).isEqualTo(PaymentStatus.PENDING);
        return this;
    }

    public PaymentAssertions isCaptured() {
        assertThat(actual.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        return this;
    }

    public PaymentAssertions hasAmount(String amount) {
        assertThat(actual.getAmount())
                .isEqualByComparingTo(new BigDecimal(amount));
        return this;
    }
}
