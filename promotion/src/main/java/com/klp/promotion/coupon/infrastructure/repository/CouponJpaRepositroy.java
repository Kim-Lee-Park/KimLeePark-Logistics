package com.klp.promotion.coupon.infrastructure.repository;

import com.klp.promotion.coupon.domain.entity.Coupon;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepositroy extends JpaRepository<Coupon, Long> {

    Coupon findByCouponId(UUID couponId);
}
