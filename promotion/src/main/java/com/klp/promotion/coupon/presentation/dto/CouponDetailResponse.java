package com.klp.promotion.coupon.presentation.dto;

import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.enums.CouponType;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponDetailResponse(
    UUID couponId,
    String name,
    CouponType discount_type,
    Long discount_value,
    int min_amount,
    Long max_discount_amount,
    Long total_quantity,
    Long remain_quantity,
    LocalDateTime expired_at
){

    public static CouponDetailResponse from (Coupon coupon){
        return new CouponDetailResponse(
            coupon.getCouponId(),
            coupon.getName(),
            coupon.getDiscount_type(),
            coupon.getDiscount_value(),
            coupon.getMin_amount(),
            coupon.getMax_discount_amount(),
            coupon.getTotal_quantity(),
            coupon.getRemain_quantity(),
            coupon.getExpired_at()
        );
    }

}