package com.klp.promotion.common.util;

import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.global.exception.BusinessException;
import com.klp.promotion.grade.domain.entity.Grade;

public class PriceCalculator {

    // 쿠폰 할인 금액
    public static Long calculateCouponDiscount(Coupon coupon, int orderPrice) {
        // 주문금액이 최소 결제금액보다 낮을때
        if(orderPrice < coupon.getMin_amount()){
            throw new BusinessException(CouponErrorCode.COUPON_MIN_AMOUNT_NOT_MET);
        }

        return switch (coupon.getDiscount_type()) {
            case FIXED -> coupon.getDiscount_value();
            case RATE -> {
                double couponRate = coupon.getDiscount_value() / 100.0;
                long couponDiscount = Math.round(orderPrice * couponRate);

                // 쿠폰 할인 금액이 최대 할인 금액을 넘었을때
                if(couponDiscount > coupon.getMax_discount_amount()){
                    couponDiscount = coupon.getMax_discount_amount();
                }
                yield couponDiscount;
            }
        };
    }


    // 등급 할인 금액 계산
    public static Long calculateGradeDiscount(Grade grade, Long amountAfterCoupon) {
        Long gradeDiscountRate = Long.valueOf(grade.getBenefitDiscountRate());
        double gradeRate = gradeDiscountRate / 100.0;
        // 등급 할인 금액 = 쿠폰 할인 후 금액 * 등급 할인률
        return Math.round(amountAfterCoupon * gradeRate);
    }


    // 최종 주문금액 계산
    public static DiscountResult calculateFinalPrice(Grade grade, Coupon coupon, int orderPrice) {
        // 쿠폰 할인 금액 계산
        Long couponDiscount = calculateCouponDiscount(coupon, orderPrice);
        
        // 쿠폰 할인 후 금액
        Long amountAfterCoupon = Math.max(orderPrice - couponDiscount, 0L);
        
        // 등급 할인 금액 계산
        Long gradeDiscount = calculateGradeDiscount(grade, amountAfterCoupon);
        
        // 최종 주문 금액 = 쿠폰 할인 후 금액 - 등급 할인 금액
        Long orderAmount = Math.max(amountAfterCoupon - gradeDiscount, 0L);

        return DiscountResult.from(couponDiscount, gradeDiscount, orderAmount);
    }

    public static DiscountResult calculator(Grade grade, Coupon coupon, int orderPrice) {
        return calculateFinalPrice(grade, coupon, orderPrice);
    }

}
