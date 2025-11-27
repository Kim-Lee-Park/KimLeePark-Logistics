package com.klp.delivery.common.exception;

import com.klp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ExternalApiErrorCode implements ErrorCode {
    HUB_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "허브 서비스 호출 중 오류가 발생했습니다."),
    COMPANY_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "컴퍼니 서비스 호출 중 오류가 발생했습니다."),
    COMPANY_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "컴퍼니 서비스 호출 에러 BAD_REQUEST"),
    COMPANY_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "컴퍼니 서비스 호출 에러 인증 실패"),
    COMPANY_SERVICE__FORBIDDEN(HttpStatus.FORBIDDEN, "컴퍼니 서비스 호출 에러 인가 실패"),
    DRIVER_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "컴퍼니 서비스 호출 중 오류가 발생했습니다."),
    DRIVER_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "컴퍼니 서비스 호출 에러 BAD_REQUEST"),
    DRIVER_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "컴퍼니 서비스 호출 에러 인증 실패"),
    DRIVER_SERVICE__FORBIDDEN(HttpStatus.FORBIDDEN, "컴퍼니 서비스 호출 에러 인가 실패")
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

    public String getErrorCode(){
        return this.name();
    }
}
