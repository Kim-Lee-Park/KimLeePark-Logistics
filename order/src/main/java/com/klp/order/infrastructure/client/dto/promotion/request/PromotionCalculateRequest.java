package com.klp.order.infrastructure.client.dto.promotion.request;

import java.util.UUID;

public record PromotionCalculateRequest(
    UUID userCouponId,
    String gradeName,
    int originalPrice

) {

}
