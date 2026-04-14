package com.xyz.investment;

import lombok.Getter;

@Getter
public class FraudDetectedException extends RuntimeException {

    private final String reason;

    public FraudDetectedException(String reason) {
        super(reason);
        this.reason = reason;
    }
}
