package com.xyz.investment;

import lombok.Value;

@Value
public class FraudResult {
    boolean highRisk;
    String reason;

    public static FraudResult safe() {
        return new FraudResult(false, null);
    }

    public static FraudResult highRisk(String reason) {
        return new FraudResult(true, reason);
    }
}
