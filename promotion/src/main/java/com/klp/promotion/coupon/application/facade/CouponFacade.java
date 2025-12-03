package com.klp.promotion.coupon.application.facade;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_OUT_OF_STOCK;

import com.klp.common.exception.BusinessException;
import com.klp.promotion.coupon.application.service.CouponService;
import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.presentation.dto.CreateCouponResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponFacade {

    private final UserCouponService userCouponService;
    private final CouponService couponService;

    @Transactional
    public CreateCouponResponse createCoupon(UUID couponId, Long userId) {

        UserCoupon userCoupon = userCouponService.findByUserIdAndCouponId(userId, couponId);

        if(userCoupon != null){
            throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }

       boolean result = couponService.decreaseStock(couponId);

        if(!result){
            throw new BusinessException(COUPON_OUT_OF_STOCK);
        }

        UserCoupon usercoupon = userCouponService.createUserCoupon(userId, couponId);

        Coupon coupon = couponService.updateStock(couponId);

        return new CreateCouponResponse(coupon.getCouponId(), usercoupon.getUserCouponId());
    }

}
