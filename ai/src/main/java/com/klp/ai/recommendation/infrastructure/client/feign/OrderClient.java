package com.klp.ai.recommendation.infrastructure.client.feign;

import com.klp.ai.global.config.OrderFeignClientConfig;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.OrderResponse;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.ReviewListResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", url = "${clients.order.url:}", configuration = OrderFeignClientConfig.class)
public interface OrderClient {

    @GetMapping("/v1/orders/{orderId}")
    OrderResponse getOrder(@PathVariable UUID orderId);

    @GetMapping("/v1/reviews/products/{productId}")
    ReviewListResponse getReviews(@PathVariable UUID productId);
}
