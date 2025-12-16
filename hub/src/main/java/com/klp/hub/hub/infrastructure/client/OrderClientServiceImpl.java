package com.klp.hub.hub.infrastructure.client;

import com.klp.hub.hub.application.service.OrderClientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderClientServiceImpl implements OrderClientService {

    private final OrderFeignClient orderFeignClient;

    @Override
    @CircuitBreaker(name = "orderService")
    @Retry(name = "orderService")
    public boolean hasProgressingOrders(UUID hubId) {
        return orderFeignClient.hasProgressingOrder(hubId);
    }
}
