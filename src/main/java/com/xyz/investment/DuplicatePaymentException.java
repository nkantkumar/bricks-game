package com.xyz.investment;

public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(String idempotencyKey) {
        super("Duplicate idempotency key: " + idempotencyKey);
    }
}
