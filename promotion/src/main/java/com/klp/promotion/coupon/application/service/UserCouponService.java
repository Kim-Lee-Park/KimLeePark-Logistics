package com.klp.promotion.coupon.application.service;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.CANNOT_USE_UNRESERVED_COUPON;

import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.coupon.domain.repository.UserCouponRepository;
import com.klp.promotion.global.exception.BusinessException;
import java.time.LocalDateTime;
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


    // 쿠폰 선점
    public int reserve(UUID userCouponId,  Integer version) {
        return userCouponRepository.reserve(userCouponId, version);
    }

    // 쿠폰 선점 해제
    @Transactional
    public void cancelReserve(UUID userCouponId) {
        UserCoupon userCoupon = findByUserCouponId(userCouponId);

        if (userCoupon.getStatus() != UserCouponStatus.RESERVE) {
            throw new BusinessException(CANNOT_USE_UNRESERVED_COUPON);
        }
        userCoupon.releaseReserve();
    }

    @Transactional
    public void markExpiredCoupons(LocalDateTime todayStart) {
        userCouponRepository.markExpiredCoupons(todayStart);
    }
}
