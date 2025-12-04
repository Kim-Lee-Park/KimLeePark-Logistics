package com.klp.promotion.coupon.infrastructure.repository;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_NOT_FOUND;

import com.klp.common.exception.BusinessException;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.repository.CouponRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final StringRedisTemplate redisTemplate;
    private final CouponJpaRepositroy couponJpaRepositroy;
    private final String COUPON_KEY = "coupon:stock:";

    @Override
    public boolean decreaseStock(UUID couponId) {

        String key = COUPON_KEY + couponId;

        // 키 존재 여부 확인
        if (!redisTemplate.hasKey(key)) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        Long remain = redisTemplate.opsForValue().decrement(key);

        if (remain >= 0) {
            return true;
        }

        // 음수면 재고 부족 시 원상복구
        redisTemplate.opsForValue().increment(key);

        return false;
    }

    @Override
    public Coupon findByCouponId(UUID couponId) {
        return couponJpaRepositroy.findByCouponId(couponId);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponJpaRepositroy.save(coupon);
    }
}
