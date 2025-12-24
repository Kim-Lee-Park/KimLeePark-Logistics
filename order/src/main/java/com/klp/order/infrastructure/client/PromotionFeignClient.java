package com.klp.order.infrastructure.client;

import com.klp.global.config.FeignTracingConfig;
import com.klp.global.config.PromotionFeignClientConfig;
import com.klp.order.infrastructure.client.dto.promotion.request.PromotionCalculateRequest;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "promotion-service", url = "${clients.promotion.url:}", configuration = {
    PromotionFeignClientConfig.class,
    FeignTracingConfig.class})
public interface PromotionFeignClient {

    @PostMapping("/v1/internal/promotions/user/coupon/apply")
    PromotionResponse getPromotionInfo(@RequestBody PromotionCalculateRequest request);
}
