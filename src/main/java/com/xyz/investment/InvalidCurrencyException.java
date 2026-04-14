package com.xyz.investment;

public class InvalidCurrencyException extends RuntimeException {

    public InvalidCurrencyException(String currency) {
        super("Invalid currency: " + currency);
    }
}
