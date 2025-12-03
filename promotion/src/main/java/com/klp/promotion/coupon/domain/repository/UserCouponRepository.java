package com.klp.promotion.coupon.domain.repository;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import java.util.UUID;

public interface UserCouponRepository {

    UserCoupon findByUserIdAndCouponId(Long userId, UUID couponId);

    UserCoupon save(UserCoupon userCoupon);
}
