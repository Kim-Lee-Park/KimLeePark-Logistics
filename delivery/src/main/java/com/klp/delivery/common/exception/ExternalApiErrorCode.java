package com.klp.delivery.common.exception;

import com.klp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ExternalApiErrorCode implements ErrorCode {
    HUB_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "허브 서비스 호출 중 오류가 발생했습니다."),
    HUB_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "허브 서비스 호출 에러 BAD_REQUEST"),
    HUB_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "허브 서비스 호출 에러 인증 실패"),
    HUB_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "허브 서비스 호출 에러 인가 실패");

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
