package com.klp.promotion.coupon.presentation.dto;

import java.util.UUID;

public record CouponApplyRequest(
    UUID userCouponId,
    String gradeName,
    int originalPrice
) {

}
