package com.klp.order.infrastructure.client.dto.promotion.request;

import java.util.UUID;

public record PromotionCalculateRequest(
    String grade,
    int price,
    UUID couponId
) {

}
