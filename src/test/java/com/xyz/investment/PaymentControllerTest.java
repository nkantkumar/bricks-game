package com.xyz.investment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc
@Import({ApiKeyAuthFilter.class, GlobalExceptionHandler.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /v1/payments → 201 created")
    void shouldCreatePayment() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-1")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .paymentId("pay-123")
                .status(PaymentStatus.PENDING)
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .createdAt(Instant.now())
                .build();

        given(paymentService.createPayment(any(PaymentRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", "pk_test_abc123")
                        .header("X-Idempotency-Key", "idem-key-1")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("pay-123"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("SGD"))
                .andDo(print());
    }

    @Test
    @DisplayName("POST /v1/payments → 400 bad request when amount missing")
    void shouldReturn400WhenAmountMissing() throws Exception {
        String requestJson = """
            {
                "currency": "SGD",
                "merchantId": "merchant-123",
                "idempotencyKey": "idem-key-1"
            }
            """;

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", "pk_test_abc123")
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.amount").exists());
    }

    @Test
    @DisplayName("POST /v1/payments → 409 conflict on duplicate")
    void shouldReturn409OnDuplicate() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("duplicate-key")
                .build();

        given(paymentService.createPayment(any()))
                .willThrow(new DuplicatePaymentException("duplicate-key"));

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", "pk_test_abc123")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_PAYMENT"));
    }

    @Test
    @DisplayName("POST /v1/payments → 401 when API key missing")
    void shouldReturn401WhenApiKeyMissing() throws Exception {
        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /v1/payments → 422 when fraud detected")
    void shouldReturn422WhenFraudDetected() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("99999.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("idem-key-fraud")
                .build();

        given(paymentService.createPayment(any()))
                .willThrow(new FraudDetectedException("VELOCITY_CHECK_FAILED"));

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", "pk_test_abc123")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("FRAUD_DETECTED"))
                .andExpect(jsonPath("$.reason").value("VELOCITY_CHECK_FAILED"));
    }

    @Test
    @DisplayName("GET /v1/payments/{id} → 200 ok")
    void shouldGetPayment() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .paymentId("pay-123")
                .status(PaymentStatus.CAPTURED)
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .build();

        given(paymentService.getPayment("pay-123")).willReturn(response);

        mockMvc.perform(get("/v1/payments/pay-123")
                        .header("X-API-Key", "pk_test_abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("pay-123"))
                .andExpect(jsonPath("$.status").value("CAPTURED"));
    }

    @Test
    @DisplayName("GET /v1/payments/{id} → 404 not found")
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        given(paymentService.getPayment("unknown-id"))
                .willThrow(new PaymentNotFoundException("unknown-id"));

        mockMvc.perform(get("/v1/payments/unknown-id")
                        .header("X-API-Key", "pk_test_abc123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PAYMENT_NOT_FOUND"));
    }
}
