package com.klp.delivery.common.exception;

import com.klp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ExternalApiErrorCode implements ErrorCode {
    HUB_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "허브 서비스 호출 중 오류가 발생했습니다."),
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
