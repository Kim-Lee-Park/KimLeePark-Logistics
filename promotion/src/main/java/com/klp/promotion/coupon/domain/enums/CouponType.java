package com.klp.promotion.coupon.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponType {

    FIXED("정액 할인"),
    RATE("정률 할인");

    private final String description;
}
