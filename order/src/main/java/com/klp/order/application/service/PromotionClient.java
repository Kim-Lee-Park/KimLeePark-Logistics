package com.klp.order.application.service;

import com.klp.order.infrastructure.client.dto.promotion.request.PromotionCalculateRequest;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;

public interface PromotionClient {

    PromotionResponse getPromotionInfo(PromotionCalculateRequest request);
}
