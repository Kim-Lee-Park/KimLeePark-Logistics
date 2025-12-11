package com.klp.promotion.coupon.application.facade;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_NOT_FOUND;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_OUT_OF_STOCK;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_VERSION_MISMATCH;

import com.klp.promotion.common.util.PriceCalculator;
import com.klp.promotion.coupon.application.service.CouponService;
import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.coupon.presentation.dto.CouponApplyResponse;
import com.klp.promotion.coupon.presentation.dto.IssueUserCouponResponse;
import com.klp.promotion.global.exception.BusinessException;
import com.klp.promotion.grade.application.GradeService;
import com.klp.promotion.grade.domain.entity.Grade;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponFacade {

    private final UserCouponService userCouponService;
    private final CouponService couponService;
    private final GradeService gradeService;

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

    @Transactional
    public CouponApplyResponse applyUserCoupon(UUID userCouponId, String gradeName,
        int originalPrice) {
        // 쿠폰 조회
        UserCoupon userCoupon = userCouponService.findByUserCouponId(userCouponId);
        UserCoupon.validateUsable(userCoupon);

        // 쿠폰선점
        int lock = userCouponService.reserve(userCouponId, userCoupon.getVersion());

        if (lock == 0) {
            throw new BusinessException(COUPON_VERSION_MISMATCH);
        }

        try {
            Coupon coupon = couponService.findByCouponId(userCoupon.getCouponId());

            Grade grade = gradeService.getGradeByName(gradeName);

            return CouponApplyResponse.from(PriceCalculator.calculator(grade, coupon, originalPrice));

        } catch (Exception e) {
            // 시스템 예외 발생 시 쿠폰 선점 원복
            log.error("쿠폰 할인 계산 실패: userCouponId={}, gradeName={}, originalPrice={}",
                userCouponId, gradeName, originalPrice, e);
            // @Modifying 쿼리로 인해 영속성 컨텍스트가 클리어되었으므로 다시 조회
            userCoupon = userCouponService.findByUserCouponId(userCouponId);
            userCoupon.releaseReserve();

            throw new BusinessException(CouponErrorCode.COUPON_CALCULATION_FAILED);
        }
    }
}
