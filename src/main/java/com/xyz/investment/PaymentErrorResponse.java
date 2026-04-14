package com.xyz.investment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentErrorResponse {
    private String error;
    private String message;
    private String reason;
    private Map<String, String> fields;
}
