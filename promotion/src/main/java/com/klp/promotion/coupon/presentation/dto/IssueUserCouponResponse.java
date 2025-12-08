package com.klp.promotion.coupon.presentation.dto;

import java.util.UUID;

public record IssueUserCouponResponse(
    UUID couponId,
    UUID userCouponId
)  {

}

