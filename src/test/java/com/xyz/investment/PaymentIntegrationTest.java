package com.xyz.investment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EmbeddedKafka(partitions = 1, topics = {"payment-created"})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("wiremock")
class PaymentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("payment_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository repository;

    @BeforeEach
    void stubFraudServiceSafe() {
        stubFor(post(urlEqualTo("/v1/fraud/check"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"SAFE\",\"score\":0.1}")));
    }

    @Test
    @Order(1)
    @DisplayName("full payment creation flow")
    void shouldCreatePaymentEndToEnd() {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("500.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("e2e-idem-key-1")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", "pk_test_abc123");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<PaymentResponse> response = restTemplate.exchange(
                "/v1/payments",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                PaymentResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentId()).isNotNull();
        assertThat(response.getBody().getStatus())
                .isEqualTo(PaymentStatus.PENDING);

        Optional<Payment> saved = repository.findById(
                response.getBody().getPaymentId()
        );
        assertThat(saved).isPresent();
        assertThat(saved.get().getAmount())
                .isEqualByComparingTo("500.00");
    }

    @Test
    @Order(2)
    @DisplayName("idempotency — same key returns same result")
    void shouldBeIdempotent() {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("idempotent-key-1")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", "pk_test_abc123");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<PaymentResponse> first = restTemplate.exchange(
                "/v1/payments", HttpMethod.POST,
                new HttpEntity<>(request, headers), PaymentResponse.class
        );

        ResponseEntity<PaymentResponse> second = restTemplate.exchange(
                "/v1/payments", HttpMethod.POST,
                new HttpEntity<>(request, headers), PaymentResponse.class
        );

        assertThat(first.getBody().getPaymentId())
                .isEqualTo(second.getBody().getPaymentId());

        long count = repository.countByIdempotencyKey("idempotent-key-1");
        assertThat(count).isEqualTo(1);
    }
}
