package com.klp.promotion.coupon.common.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

    COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "쿠폰이 이미 발급되어 있습니다"),
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 쿠폰이 없습니다"),
    COUPON_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "쿠폰 재고가 없습니다")
    ;

    private final HttpStatus status;
    private final String message;


}
