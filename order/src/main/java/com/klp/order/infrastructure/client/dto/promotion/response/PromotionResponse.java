package com.klp.order.infrastructure.client.dto.promotion.response;

public record PromotionResponse(
    int gradeDiscountPrice,
    int couponDiscountPrice,
    int totalDiscount
) {

}
