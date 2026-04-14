package com.xyz.investment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PaymentErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid"
                ));

        return ResponseEntity.badRequest()
                .body(PaymentErrorResponse.builder()
                        .error("VALIDATION_ERROR")
                        .fields(fields)
                        .build());
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<PaymentErrorResponse> handleDuplicate(DuplicatePaymentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(PaymentErrorResponse.builder()
                        .error("DUPLICATE_PAYMENT")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(FraudDetectedException.class)
    public ResponseEntity<PaymentErrorResponse> handleFraud(FraudDetectedException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(PaymentErrorResponse.builder()
                        .error("FRAUD_DETECTED")
                        .reason(ex.getReason())
                        .build());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<PaymentErrorResponse> handleNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(PaymentErrorResponse.builder()
                        .error("PAYMENT_NOT_FOUND")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(FraudServiceUnavailableException.class)
    public ResponseEntity<PaymentErrorResponse> handleFraudUnavailable(
            FraudServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(PaymentErrorResponse.builder()
                        .error("FRAUD_SERVICE_UNAVAILABLE")
                        .message(ex.getMessage())
                        .build());
    }
}
