package com.klp.order.common.exception;

import com.klp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ExternalApiErrorCode implements ErrorCode {

    DELIVERY_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "배송 서비스 호출 중 오류가 발생했습니다."),
    DELIVERY_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "배송 서비스 호출 에러 BAD_REQUEST"),
    DELIVERY_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "배송 서비스 호출 에러 인증 실패"),
    DELIVERY_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "배송 서비스 호출 에러 인가 실패"),
    INVENTORY_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "배송 서비스 호출 중 오류가 발생했습니다."),
    INVENTORY_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "배송 서비스 호출 에러 BAD_REQUEST"),
    INVENTORY_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "배송 서비스 호출 에러 인증 실패"),
    INVENTORY_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "배송 서비스 호출 에러 인가 실패");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return this.name();
    }


}
