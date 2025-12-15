package com.klp.promotion.coupon.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserCouponStatus {
    READY("사용전"),
    RESERVE("쿠폰선점"),
    USED("사용"),
    EXPIRED("유효기간만료");

    private final String description;
}
