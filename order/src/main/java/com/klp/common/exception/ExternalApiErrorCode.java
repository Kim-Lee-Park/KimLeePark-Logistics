package com.klp.common.exception;

import com.klp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ExternalApiErrorCode implements ErrorCode {

    DELIVERY_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "배송 서비스 호출 중 오류가 발생했습니다."),
    DELIVERY_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "배송 서비스 호출 에러 BAD_REQUEST"),
    DELIVERY_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "배송 서비스 호출 에러 인증 실패"),
    DELIVERY_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "배송 서비스 호출 에러 인가 실패"),
    INVENTORY_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "INVENTORY 서비스 호출 중 오류가 발생했습니다."),
    INVENTORY_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "INVENTORY 서비스 호출 에러 BAD_REQUEST"),
    INVENTORY_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "INVENTORY 서비스 호출 에러 인증 실패"),
    INVENTORY_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "INVENTORY 서비스 호출 에러 인가 실패"),
    USER_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "USER 서비스 호출 중 오류가 발생했습니다."),
    USER_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "USER 서비스 호출 에러 BAD_REQUEST"),
    USER_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "USER 서비스 호출 에러 인증 실패"),
    USER_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "USER 서비스 호출 에러 인가 실패"),
    PRODUCT_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "PRODUCT 서비스 호출 중 오류가 발생했습니다."),
    PROMOTION_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "프로모션 서비스 호출 중 오류가 발생했습니다."),
    PROMOTION_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "프로모션 서비스 호출 에러 BAD_REQUEST"),
    PROMOTION_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "프로모션 서비스 호출 에러 인증 실패"),
    PROMOTION_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "프로모션 서비스 호출 에러 인가 실패"),
    ;

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
