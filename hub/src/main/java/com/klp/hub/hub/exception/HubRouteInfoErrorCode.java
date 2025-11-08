package com.klp.hub.hub.exception;

import com.klp.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum HubRouteInfoErrorCode implements ErrorCode {
    ;

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
