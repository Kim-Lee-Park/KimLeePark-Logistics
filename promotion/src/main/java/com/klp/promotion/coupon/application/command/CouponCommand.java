package com.klp.promotion.coupon.application.command;

import com.klp.promotion.coupon.domain.enums.CouponType;
import java.time.LocalDateTime;

public record CouponCommand(
    String name,
    CouponType discount_type,
    Long discount_value,
    int min_amount,
    Long max_discount_amount,
    Long total_quantity,
    LocalDateTime expired_at
) {

}
