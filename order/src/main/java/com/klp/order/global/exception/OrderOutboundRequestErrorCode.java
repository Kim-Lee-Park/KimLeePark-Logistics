package com.klp.order.global.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderOutboundRequestErrorCode implements ErrorCode {
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "요청 정보를 찾을 수 없습니다."),
    REQUEST_BY_IDEMPOTENCY_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 멱등키의 요청을 찾을 수 없습니다."),
    REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "요청 정보는 필수입니다."),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "멱등키는 필수입니다."),
    TARGET_REQUIRED(HttpStatus.BAD_REQUEST, "요청 대상은 필수입니다."),
    OPERATION_REQUIRED(HttpStatus.BAD_REQUEST, "요청 작업은 필수입니다."),
    IDEMPOTENCY_KEY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 멱등키 입니다."),
    ORDER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "주문 ID는 필수입니다."),
    TARGET_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "타겟은 필수입니다."),
    OPERATION_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "작업 타입은 필수입니다."),
    OREDER_REQUIRED(HttpStatus.BAD_REQUEST, "주문 정보는 필수입니다.");

    private final HttpStatus status;
    private final String message;
}
