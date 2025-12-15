package com.klp.promotion.coupon.presentation.dto;

import com.klp.promotion.common.util.DiscountResult;

public record CouponApplyResponse(
    int couponDiscountPrice,
    int gradeDiscountPrice,
    int orderPrice
) {

    public static CouponApplyResponse from(DiscountResult  result) {
        return new CouponApplyResponse(result.couponDiscountPrice(), result.gradeDiscountPrice(), result.orderPrice());
    }

}
