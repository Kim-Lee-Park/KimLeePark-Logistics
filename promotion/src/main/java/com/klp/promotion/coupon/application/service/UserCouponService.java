package com.klp.promotion.coupon.application.service;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.repository.UserCouponRepository;
import java.util.List;
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

    @Transactional(readOnly = true)
    public UserCoupon findByUserCouponId(UUID userCouponId) {
        return userCouponRepository.findByUserCouponId(userCouponId);
    }

    @Transactional(readOnly = true)
    public List<UserCoupon> findAllByUserId(Long userId) {
        return userCouponRepository.findAllByUserId(userId);
    }


}
