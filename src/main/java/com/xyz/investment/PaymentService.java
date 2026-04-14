package com.xyz.investment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final FraudCheckService fraudCheckService;
    private final KafkaTemplate<String, PaymentEvent> kafka;

    private static final Set<String> VALID_CURRENCIES =
            Set.of("USD", "SGD", "EUR", "GBP", "JPY");

    public PaymentResponse createPayment(PaymentRequest request) {

        if (!VALID_CURRENCIES.contains(request.getCurrency())) {
            throw new InvalidCurrencyException(request.getCurrency());
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException(request.getAmount());
        }

        Optional<Payment> existing = repository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            if (matchesIdempotentRequest(existing.get(), request)) {
                return toResponse(existing.get());
            }
            throw new DuplicatePaymentException(request.getIdempotencyKey());
        }

        FraudResult fraud = fraudCheckService.check(request);
        if (fraud.isHighRisk()) {
            throw new FraudDetectedException(fraud.getReason());
        }

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantId(request.getMerchantId())
                .idempotencyKey(request.getIdempotencyKey())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        Payment saved = repository.save(payment);

        kafka.send("payment-created", PaymentEvent.of(saved));

        return toResponse(saved);
    }

    public PaymentResponse getPayment(String paymentId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return toResponse(payment);
    }

    private static boolean matchesIdempotentRequest(Payment stored, PaymentRequest request) {
        if (stored.getAmount() == null
                || stored.getCurrency() == null
                || stored.getMerchantId() == null) {
            return false;
        }
        return stored.getAmount().compareTo(request.getAmount()) == 0
                && Objects.equals(stored.getCurrency(), request.getCurrency())
                && Objects.equals(stored.getMerchantId(), request.getMerchantId());
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
