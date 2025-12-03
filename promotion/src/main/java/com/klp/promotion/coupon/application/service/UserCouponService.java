package com.klp.promotion.coupon.application.service;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.repository.UserCouponRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;

    @Transactional(readOnly = true)
    public UserCoupon findByUserIdAndCouponId(Long userId, UUID couponId) {
        return userCouponRepository.findByUserIdAndCouponId(userId, couponId);
    }

    public UserCoupon createUserCoupon(Long userId, UUID couponId) {

        return userCouponRepository.save(UserCoupon.create(couponId, userId));

    }
}
