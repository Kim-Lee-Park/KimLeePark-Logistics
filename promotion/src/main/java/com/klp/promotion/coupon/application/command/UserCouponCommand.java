package com.klp.promotion.coupon.application.command;

import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserCouponCommand(
    UUID userCouponId,
    UUID couponId,
    Long userId,
    UserCouponStatus status,
    LocalDateTime usedAt
) {

}
