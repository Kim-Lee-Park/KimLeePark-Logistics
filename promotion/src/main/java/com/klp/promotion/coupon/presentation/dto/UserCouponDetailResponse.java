package com.klp.promotion.coupon.presentation.dto;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserCouponDetailResponse(
    UUID userCouponId,
    UUID couponId,
    Long userId,
    UserCouponStatus status,
    LocalDateTime usedAt
) {

    public static UserCouponDetailResponse from(UserCoupon userCoupon) {
        return new UserCouponDetailResponse(
            userCoupon.getUserCouponId(),
            userCoupon.getCouponId(),
            userCoupon.getUserId(),
            userCoupon.getStatus(),
            userCoupon.getUsedAt()
        );
    }

    public static List<UserCouponDetailResponse> fromList(List<UserCoupon> userCoupons) {
        return userCoupons.stream()
            .map(UserCouponDetailResponse::from)
            .toList();
    }

}
