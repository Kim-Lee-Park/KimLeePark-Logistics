package com.klp.promotion.coupon.domain.repository;

import com.klp.promotion.coupon.domain.entity.Coupon;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponRepository {

    boolean decreaseStock(UUID couponId);

    Coupon findByCouponId(UUID couponId);

    Coupon save(Coupon coupon);

    Page<Coupon> findAll(Pageable page);
}
