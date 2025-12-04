package com.klp.promotion.coupon.domain.repository;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import java.util.List;
import java.util.UUID;

public interface UserCouponRepository {

    UserCoupon findByUserIdAndCouponId(Long userId, UUID couponId);

    UserCoupon save(UserCoupon userCoupon);

    UserCoupon findByUserCouponId(UUID userCouponId);

    List<UserCoupon> findAllByUserId(Long userId);
}
