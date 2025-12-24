package com.klp.promotion.coupon.infrastructure.repository;

import com.klp.promotion.coupon.domain.entity.QCoupon;
import com.klp.promotion.coupon.domain.entity.QUserCoupon;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.coupon.domain.repository.UserCouponRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepotiory userCouponJpaRepotiory;
    private final JPAQueryFactory queryFactory;

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

    @Override
    public int reserve(UUID userCouponId, Integer version) {
        return userCouponJpaRepotiory.reserve(userCouponId, version);
    }

    @Override
    public void markExpiredCoupons(LocalDateTime todayStart) {
        QUserCoupon userCoupon = QUserCoupon.userCoupon;
        QCoupon coupon = QCoupon.coupon;

        queryFactory
            .update(userCoupon)
            .set(userCoupon.status, UserCouponStatus.EXPIRED)
            .where(
                userCoupon.status.eq(UserCouponStatus.READY),
                userCoupon.couponId.in(
                    JPAExpressions
                        .select(coupon.couponId)
                        .from(coupon)
                        .where(coupon.expired_at.lt(todayStart))
                )
            )
            .execute();
    }
}
