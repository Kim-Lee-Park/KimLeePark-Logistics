package com.klp.promotion.coupon.infrastructure.repository;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCouponJpaRepotiory extends JpaRepository<UserCoupon, UUID> {

    UserCoupon findByUserIdAndCouponId(Long userId, UUID couponId);

    UserCoupon findByUserCouponId(UUID userCouponId);

    List<UserCoupon> findAllByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE UserCoupon c
            SET c.status = 'RESERVE',
                c.version = c.version + 1
            WHERE c.userCouponId = :userCouponId
              AND c.version = :version
              AND c.status = 'READY'
        """)
    int reserve(@Param("userCouponId") UUID userCouponId, @Param("version") Integer version);
}
