package com.klp.hub.hub.exception;

import com.klp.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum HubRouteInfoErrorCode implements ErrorCode {
    NOT_EXISTS(HttpStatus.NOT_FOUND,"존재하지 않는 허브간 이동 정보입니다.");

    private HttpStatus httpStatus;
    private String message;

    HubRouteInfoErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus=httpStatus;
        this.message=message;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ErrorCode appendMessage(String appendMessage) {
        this.message+=appendMessage;
        return this;
    }
}
