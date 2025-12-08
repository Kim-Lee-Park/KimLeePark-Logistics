package com.klp.promotion.coupon.infrastructure.repository;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.repository.UserCouponRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepotiory userCouponJpaRepotiory;

    @Override
    public UserCoupon findByUserIdAndCouponId(Long userId, UUID couponId) {
        return userCouponJpaRepotiory.findByUserIdAndCouponId(userId, couponId);
    }

    public UserCoupon save(UserCoupon userCoupon) {
        return userCouponJpaRepotiory.save(userCoupon);
    }

    @Override
    public UserCoupon findByUserCouponId(UUID userCouponId) {
        return userCouponJpaRepotiory.findByUserCouponId(userCouponId);
    }

    @Override
    public List<UserCoupon> findAllByUserId(Long userId) {
        return userCouponJpaRepotiory.findAllByUserId(userId);
    }
}
