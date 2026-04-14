package com.xyz.investment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository repository;

    @Mock
    private FraudCheckService fraudCheckService;

    @Mock
    private KafkaTemplate<String, PaymentEvent> kafka;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("should create payment successfully")
    void shouldCreatePaymentSuccessfully() {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-1")
                .build();

        Payment savedPayment = Payment.builder()
                .paymentId("pay-123")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantId(request.getMerchantId())
                .status(PaymentStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .createdAt(Instant.now())
                .build();

        given(fraudCheckService.check(request))
                .willReturn(FraudResult.safe());
        given(repository.save(any(Payment.class)))
                .willReturn(savedPayment);

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo("pay-123");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.getAmount()).isEqualByComparingTo("100.00");

        verify(fraudCheckService).check(request);
        verify(repository).save(any(Payment.class));
        verify(kafka).send(eq("payment-created"), any(PaymentEvent.class));
    }

    @Test
    @DisplayName("should reject payment when fraud detected")
    void shouldRejectPaymentWhenFraudDetected() {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("99999.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-2")
                .build();

        given(fraudCheckService.check(request))
                .willReturn(FraudResult.highRisk("VELOCITY_CHECK_FAILED"));

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(FraudDetectedException.class)
                .hasMessageContaining("VELOCITY_CHECK_FAILED");

        verify(repository, never()).save(any());
        verify(kafka, never()).send(any(), any());
    }

    @Test
    @DisplayName("should throw exception for duplicate idempotency key")
    void shouldThrowForDuplicateIdempotencyKey() {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("duplicate-key")
                .build();

        given(repository.findByIdempotencyKey("duplicate-key"))
                .willReturn(Optional.of(Payment.builder()
                        .paymentId("existing-pay")
                        .status(PaymentStatus.PENDING)
                        .build()));

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining("duplicate-key");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("should return existing payment for duplicate idempotency key")
    void shouldReturnExistingForDuplicateKey() {
        Payment existing = Payment.builder()
                .paymentId("existing-pay-123")
                .status(PaymentStatus.CAPTURED)
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .build();

        given(repository.findByIdempotencyKey("idem-key-1"))
                .willReturn(Optional.of(existing));

        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-1")
                .build();

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response.getPaymentId()).isEqualTo("existing-pay-123");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("should reject zero amount")
    void shouldRejectZeroAmount() {
        PaymentRequest request = PaymentRequest.builder()
                .amount(BigDecimal.ZERO)
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-3")
                .build();

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(InvalidPaymentAmountException.class);
    }

    @Test
    @DisplayName("should reject negative amount")
    void shouldRejectNegativeAmount() {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("-100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-4")
                .build();

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(InvalidPaymentAmountException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "SGD", "EUR", "GBP"})
    @DisplayName("should accept valid currencies")
    void shouldAcceptValidCurrencies(String currency) {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency(currency)
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-" + currency)
                .build();

        given(fraudCheckService.check(any())).willReturn(FraudResult.safe());
        given(repository.save(any())).willReturn(buildPayment(currency));

        assertThatNoException()
                .isThrownBy(() -> paymentService.createPayment(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "US", "SGDD", ""})
    @DisplayName("should reject invalid currencies")
    void shouldRejectInvalidCurrencies(String currency) {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency(currency)
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-x")
                .build();

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(InvalidCurrencyException.class);
    }

    private Payment buildPayment(String currency) {
        return Payment.builder()
                .paymentId("pay-" + currency)
                .amount(new BigDecimal("100.00"))
                .currency(currency)
                .merchantId("merchant-123")
                .status(PaymentStatus.PENDING)
                .idempotencyKey("idem-" + currency)
                .createdAt(Instant.now())
                .build();
    }
}
