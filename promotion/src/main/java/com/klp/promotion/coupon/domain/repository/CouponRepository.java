package com.klp.promotion.coupon.domain.repository;

import com.klp.promotion.coupon.domain.entity.Coupon;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponRepository {

    boolean decreaseStock(UUID couponId);

    Coupon findByCouponId(UUID couponId);

    Coupon save(Coupon coupon);

    void saveStockToRedis(UUID couponId, Long stock);

    Page<Coupon> findAll(Pageable page);
}
