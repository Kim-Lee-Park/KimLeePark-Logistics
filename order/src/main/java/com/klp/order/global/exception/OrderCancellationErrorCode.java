package com.klp.order.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderCancellationErrorCode implements ErrorCode {

    CANCELLATION_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 취소 정보를 찾을 수 없습니다."),
    CANCELLATION_BY_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문의 취소 정보를 찾을 수 없습니다."),
    CANCELLATION_REQUIRED(HttpStatus.BAD_REQUEST, "취소 정보는 필수입니다."),
    ORDER_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "주문 정보는 필수입니다."),
    CANCELLED_BY_REQUIRED(HttpStatus.BAD_REQUEST, "취소자 정보는 필수입니다."),
    CANCEL_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "취소 유형은 필수입니다."),
    ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 주문 입니다.");

    private final HttpStatus status;
    private final String message;
}
