package com.klp.promotion.coupon.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.global.exception.BusinessException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
class CouponRepositoryImplTest {

    @Autowired
    private CouponRepositoryImpl couponRepositoryImpl;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        Set<String> keys = redisTemplate.keys("coupon:stock:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("재고 차감 성공")
    void decreaseStock_Success() {
        // given
        UUID couponId = UUID.randomUUID();
        String key = "coupon:stock:" + couponId;
        redisTemplate.opsForValue().set(key, "10");

        // when
        boolean result = couponRepositoryImpl.decreaseStock(couponId);

        // then
        assertThat(result).isTrue();
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("9");
    }

    @Test
    @DisplayName("재고 부족으로 차감 실패 및 원상복구")
    void decreaseStock_Fail_OutOfStock() {
        // given
        UUID couponId = UUID.randomUUID();
        String key = "coupon:stock:" + couponId;
        redisTemplate.opsForValue().set(key, "0");

        // when
        boolean result = couponRepositoryImpl.decreaseStock(couponId);

        // then
        assertThat(result).isFalse();
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("0");
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않을 때 예외 발생")
    void decreaseStock_Fail_CouponNotFound() {
        // given
        UUID couponId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> couponRepositoryImpl.decreaseStock(couponId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", CouponErrorCode.COUPON_NOT_FOUND);
    }
}

