package com.klp.promotion.coupon.domain.repository;

import com.klp.promotion.coupon.domain.entity.Coupon;
import java.util.UUID;

public interface CouponRepository {

    boolean decreaseStock(UUID couponId);

    Coupon findByCouponId(UUID couponId);

    Coupon save(Coupon coupon);
}
