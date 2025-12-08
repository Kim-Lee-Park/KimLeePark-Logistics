package com.klp.promotion.coupon.application.facade;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_NOT_FOUND;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_OUT_OF_STOCK;

import com.klp.promotion.coupon.application.service.CouponService;
import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.coupon.presentation.dto.IssueUserCouponResponse;
import com.klp.promotion.global.exception.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCouponFacade {

    private final UserCouponService userCouponService;
    private final CouponService couponService;

    @Transactional
    public IssueUserCouponResponse issueUserCoupon(UUID couponId, Long userId) {

        UserCoupon userCoupon = userCouponService.findByUserIdAndCouponId(userId, couponId);

        if (userCoupon != null) {
            throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }

        boolean result = couponService.decreaseStock(couponId);

        if (!result) {
            throw new BusinessException(COUPON_OUT_OF_STOCK);
        }

        UserCoupon usercoupon = userCouponService.createUserCoupon(userId, couponId);

        Coupon coupon = couponService.updateStock(couponId);

        return new IssueUserCouponResponse(coupon.getCouponId(), usercoupon.getUserCouponId());
    }

    @Transactional
    public void useUserCoupon(UUID couponId, Long userId) {

        UserCoupon userCoupon = userCouponService.findByUserIdAndCouponId(userId, couponId);
        if (userCoupon == null) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        if (userCoupon.getStatus() == UserCouponStatus.USED) {
            throw new BusinessException(CouponErrorCode.COUPON_ALREADY_USED);
        }

        Coupon coupon = couponService.findByCouponId(couponId);
        if (coupon == null) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        Coupon.validateExpiredAt(coupon.getExpired_at());
        userCoupon.useCoupon();
    }


    @Transactional
    public void deleteUserCoupon(UUID userCouponId, Long userId) {

        UserCoupon userCoupon = userCouponService.findByUserCouponId(userCouponId);
        if (userCoupon == null) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        if (userCoupon.isDeleted()) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        userCoupon.deleteValidate();
        userCoupon.delete(userId);
    }
}
