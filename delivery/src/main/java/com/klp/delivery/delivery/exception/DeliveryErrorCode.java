package com.klp.delivery.delivery.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {
    DUPLICATE_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST, "이미 처리된 요청입니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 호출에 실패했습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "배송을 찾을 수 없습니다."),
    INVALID_DELIVERY_DATA(HttpStatus.BAD_REQUEST, "배송 정보가 올바르지 않습니다."),
    DELIVERY_CANNOT_BE_MODIFIED(HttpStatus.BAD_REQUEST, "생성된 배송은 수정할 수 없습니다.");

    private final HttpStatus status;
    private final String message;


}

