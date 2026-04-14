package com.xyz.investment;

public interface FraudCheckService {

    FraudResult check(PaymentRequest request);
}
