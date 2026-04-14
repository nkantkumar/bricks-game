package com.xyz.investment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("wiremock")
@EmbeddedKafka(partitions = 1, topics = {"payment-created"})
class PaymentWithExternalApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("should capture payment when fraud API returns safe")
    void shouldCaptureWhenFraudApiSafe() {
        stubFor(post(urlEqualTo("/v1/fraud/check"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                        "result": "SAFE",
                        "score": 0.05
                    }
                    """)));

        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("wire-idem-1")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", "pk_test_abc123");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<PaymentResponse> response = restTemplate.exchange(
                "/v1/payments", HttpMethod.POST,
                new HttpEntity<>(request, headers), PaymentResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        verify(postRequestedFor(urlEqualTo("/v1/fraud/check")));
    }

    @Test
    @DisplayName("should reject when fraud API returns high risk")
    void shouldRejectWhenFraudApiHighRisk() {
        stubFor(post(urlEqualTo("/v1/fraud/check"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                        "result": "HIGH_RISK",
                        "score": 0.95,
                        "reason": "VELOCITY_CHECK_FAILED"
                    }
                    """)));

        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("99999.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("wire-idem-2")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", "pk_test_abc123");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<PaymentErrorResponse> response = restTemplate.exchange(
                "/v1/payments", HttpMethod.POST,
                new HttpEntity<>(request, headers), PaymentErrorResponse.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getError())
                .isEqualTo("FRAUD_DETECTED");
    }

    @Test
    @DisplayName("should handle fraud API timeout gracefully")
    void shouldHandleFraudApiTimeout() {
        stubFor(post(urlEqualTo("/v1/fraud/check"))
                .willReturn(aResponse()
                        .withFixedDelay(5000)
                        .withStatus(200)));

        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("wire-idem-3")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", "pk_test_abc123");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<PaymentErrorResponse> response = restTemplate.exchange(
                "/v1/payments", HttpMethod.POST,
                new HttpEntity<>(request, headers), PaymentErrorResponse.class
        );

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.CREATED, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
