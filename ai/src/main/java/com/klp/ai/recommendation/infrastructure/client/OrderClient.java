package com.klp.ai.recommendation.infrastructure.client;

import com.klp.ai.global.config.OrderFeignClientConfig;
import com.klp.ai.recommendation.infrastructure.client.dto.response.OrderResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", configuration = OrderFeignClientConfig.class)
public interface OrderClient {

    @GetMapping("/v1/orders/{orderId}")
    OrderResponse getOrder(@PathVariable UUID orderId);
}
