package com.xyz.investment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"payment-created", "payment-approved"}
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("wiremock")
class PaymentKafkaTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    @BeforeEach
    void setUp() throws Exception {
        stubFor(post(urlEqualTo("/v1/fraud/check"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"SAFE\",\"score\":0.1}")));

        records = new LinkedBlockingQueue<>();

        Map<String, Object> consumerProps = new HashMap<>(
                KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka)
        );

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerProps,
                        new StringDeserializer(),
                        new StringDeserializer()
                );

        ContainerProperties props = new ContainerProperties("payment-created");

        container = new KafkaMessageListenerContainer<>(consumerFactory, props);
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container,
                embeddedKafka.getPartitionsPerTopic());
    }

    @Test
    @DisplayName("should publish event to Kafka on payment creation")
    void shouldPublishKafkaEvent() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("SGD")
                .merchantId("merchant-123")
                .idempotencyKey("kafka-idem-1")
                .build();

        paymentService.createPayment(request);

        ConsumerRecord<String, String> received =
                records.poll(10, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo("payment-created");
        PaymentEvent value = objectMapper.readValue(received.value(), PaymentEvent.class);
        assertThat(value.merchantId()).isEqualTo("merchant-123");
        assertThat(value.amount()).isEqualByComparingTo("100.00");
    }
}
