package com.klp.promotion.coupon.infrastructure.repository;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_NOT_FOUND;

import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.entity.QCoupon;
import com.klp.promotion.coupon.domain.entity.QUserCoupon;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.coupon.domain.repository.CouponRepository;
import com.klp.promotion.global.exception.BusinessException;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public void saveStockToRedis(UUID couponId, Long stock) {
        String key = COUPON_KEY + couponId;
        redisTemplate.opsForValue().set(key, String.valueOf(stock));
    }

    @Override
    public Page<Coupon> findAll(Pageable pageable) {
        return couponJpaRepositroy.findAll(pageable);
    }


}
