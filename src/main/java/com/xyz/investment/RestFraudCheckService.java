package com.xyz.investment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RestFraudCheckService implements FraudCheckService {

    private final ObjectMapper objectMapper;

    @Value("${fraud.client.base-url:http://localhost:8089}")
    private String baseUrl;

    @Value("${fraud.client.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${fraud.client.read-timeout-ms:2000}")
    private int readTimeoutMs;

    @Override
    public FraudResult check(PaymentRequest request) {
        RestTemplate client = createRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("amount", request.getAmount());
        body.put("currency", request.getCurrency());
        body.put("merchantId", request.getMerchantId());
        try {
            String json = client.postForObject(
                    baseUrl + "/v1/fraud/check",
                    new HttpEntity<>(body, headers),
                    String.class
            );
            FraudApiResponse parsed = objectMapper.readValue(json, FraudApiResponse.class);
            if ("HIGH_RISK".equalsIgnoreCase(parsed.result)) {
                return FraudResult.highRisk(parsed.reason != null ? parsed.reason : "HIGH_RISK");
            }
            return FraudResult.safe();
        } catch (ResourceAccessException e) {
            throw new FraudServiceUnavailableException("Fraud service unavailable", e);
        } catch (Exception e) {
            throw new IllegalStateException("Fraud check failed", e);
        }
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FraudApiResponse {
        public String result;
        public BigDecimal score;
        public String reason;
    }
}
