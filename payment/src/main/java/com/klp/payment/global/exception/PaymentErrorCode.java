package com.klp.payment.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 결제를 찾을 수 없습니다"),
    EVENT_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 직렬화에 실패했습니다");

    private final HttpStatus status;
    private final String message;
}
