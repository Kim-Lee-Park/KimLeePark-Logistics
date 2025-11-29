package com.klp.delivery.delivery.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {
    DELIVERY_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "배송 저장에 실패했습니다."),
    DELIVERY_ROUTE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "배송경로 저장에 실패했습니다."),
    DUPLICATE_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST, "이미 처리된 요청입니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 호출에 실패했습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "배송을 찾을 수 없습니다."),
    INVALID_DELIVERY_DATA(HttpStatus.BAD_REQUEST, "배송 정보가 올바르지 않습니다."),
    DELIVERY_CANNOT_BE_MODIFIED(HttpStatus.BAD_REQUEST, "배송을 수정할 수 없습니다."),
    DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "담당자 조회에 실패 했습니다."),
    DELIVERY_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "배송 삭제에 실패했습니다."),
    ROUTE_APPLY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "배송 경로 정보 적용에 실패했습니다.");

    private final HttpStatus status;
    private final String message;


}

