package com.klp.promotion.common.util;

public record DiscountResult(

    int couponDiscountPrice,
    int gradeDiscountPrice,
    int orderPrice
) {

    public static DiscountResult from(Long couponDiscountPrice, Long gradeDiscountPrice,
        Long orderPrice) {
        return new DiscountResult(couponDiscountPrice.intValue(), gradeDiscountPrice.intValue(), orderPrice.intValue());
    }

}
