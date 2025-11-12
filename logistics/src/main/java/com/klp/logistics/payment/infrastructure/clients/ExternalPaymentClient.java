package com.klp.logistics.payment.infrastructure.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPaymentClient {

    private final RestClient restClient;

    public String payment() {
        String response = restClient.post()
            .uri("/external/payment")
            .retrieve()
            .body(String.class);
        return response;
    }
}
