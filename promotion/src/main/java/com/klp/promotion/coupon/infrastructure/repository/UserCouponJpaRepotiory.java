package com.klp.promotion.coupon.infrastructure.repository;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponJpaRepotiory extends JpaRepository<UserCoupon, UUID> {

    UserCoupon findByUserIdAndCouponId(Long userId, UUID couponId);
}
