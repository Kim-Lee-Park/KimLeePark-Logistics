package com.klp.order.infrastructure.client;

import com.klp.global.config.PromotionFeignClientConfig;
import com.klp.order.infrastructure.client.dto.promotion.request.PromotionCalculateRequest;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "promotion-service", configuration = PromotionFeignClientConfig.class)
public interface PromotionClient {

    @PostMapping("/v1/internal/promotion/calculate")
    PromotionResponse getPromotionInfo(@RequestBody PromotionCalculateRequest request);
}
