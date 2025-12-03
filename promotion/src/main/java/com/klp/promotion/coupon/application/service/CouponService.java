package com.klp.promotion.coupon.application.service;


import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.repository.CouponRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public boolean decreaseStock(UUID couponId) {

        return couponRepository.decreaseStock(couponId);

    }

    public Coupon updateStock(UUID couponId) {
        Coupon coupon = findByCouponId(couponId);
        coupon.useStock();
        return coupon;

    }

    @Transactional(readOnly = true)
    public Coupon findByCouponId(UUID couponId){
        return couponRepository.findByCouponId(couponId);
    }
}
