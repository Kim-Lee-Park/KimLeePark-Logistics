package com.klp.hub.hub.infrastructure.client;

import com.klp.hub.hub.application.service.DeliveryClientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryClientServiceImpl implements DeliveryClientService {

    private final DeliveryFeignClient deliveryFeignClient;

    @Override
    @CircuitBreaker(name = "deliveryService")
    @Retry(name = "deliveryService")
    public void deleteRoutePlansByHubId(UUID hubId) {
        deliveryFeignClient.deleteRoutePlansByHubId(hubId);
    }
}
