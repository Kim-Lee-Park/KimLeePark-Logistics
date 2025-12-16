package com.klp.promotion.coupon.infrastructure.repository;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_NOT_FOUND;

import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.repository.CouponRepository;
import com.klp.promotion.global.exception.BusinessException;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final StringRedisTemplate redisTemplate;
    private final CouponJpaRepositroy couponJpaRepositroy;
    private final DefaultRedisScript<String> updateRedisScript;
    private final String COUPON_KEY = "coupon:stock:";

    @Override
    public boolean decreaseStock(UUID couponId) {
        String key = COUPON_KEY + couponId;

        String result = redisTemplate.execute(
            updateRedisScript,
            Collections.singletonList(key)
        );

        // "-1"이면 재고 부족 또는 키 없음
        if ("-1".equals(result)) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        return true;
    }

    @Override
    public Coupon findByCouponId(UUID couponId) {
        return couponJpaRepositroy.findByCouponId(couponId);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponJpaRepositroy.save(coupon);
    }

    @Override
    public Page<Coupon> findAll(Pageable pageable) {
        return couponJpaRepositroy.findAll(pageable);
    }
}
