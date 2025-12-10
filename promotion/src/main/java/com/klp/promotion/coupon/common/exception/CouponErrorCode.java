package com.klp.promotion.coupon.common.exception;

import com.klp.promotion.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

    COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "쿠폰이 이미 발급되어 있습니다"),
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 쿠폰이 없습니다"),
    COUPON_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "쿠폰 재고가 없습니다"),
    INVALID_COUPON_DISCOUNT_TYPE(HttpStatus.BAD_REQUEST, "할인 타입에 맞지 않는 금액 설정입니다"),
    INVALID_COUPON_EXPIRED_AT(HttpStatus.BAD_REQUEST, "유효기간이 과거입니다"),
    COUPON_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "이미 발급된 쿠폰은 삭제할 수 없습니다"),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다"),
    EVENT_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 직렬화에 실패했습니다");

    private final HttpStatus status;
    private final String message;


}
