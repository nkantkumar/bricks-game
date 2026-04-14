package com.xyz.investment;

public class FraudServiceUnavailableException extends RuntimeException {

    public FraudServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
