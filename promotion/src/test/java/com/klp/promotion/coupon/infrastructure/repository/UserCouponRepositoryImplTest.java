package com.klp.promotion.coupon.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.global.config.AuditConfig;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({UserCouponRepositoryImpl.class, AuditConfig.class})
public class UserCouponRepositoryImplTest {

    @Autowired
    private UserCouponRepositoryImpl userCouponRepositoryImpl;

    @Test
    void repository가_null_아님을_검증() {
        Assertions.assertThat(userCouponRepositoryImpl).isNotNull();
    }

    @Test
    public void 유저_쿠폰_조회_성공(){

        UUID couponId = UUID.randomUUID();
        Long userId = 1L;

        // 유저 쿠폰 세팅
        UserCoupon userCoupon = UserCoupon.create(couponId, userId);

        userCouponRepositoryImpl.save(userCoupon);

        UserCoupon result = userCouponRepositoryImpl.findByUserIdAndCouponId(userId, couponId);

        assertThat(result.getUserId()).isNotNull();
        assertThat(result.getCouponId()).isEqualTo(couponId);
        assertThat(result.getStatus()).isEqualTo(UserCouponStatus.READY);

    }



}
