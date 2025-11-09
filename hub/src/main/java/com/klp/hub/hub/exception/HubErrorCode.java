package com.klp.hub.hub.exception;

import com.klp.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum HubErrorCode implements ErrorCode {
    NOT_EXISTS(HttpStatus.NOT_FOUND,"존재하지 않는 허브입니다."),
    HUB_NAME_DUPLICATED(HttpStatus.BAD_REQUEST,"중복된 허브 이름입니다."),
    HUB_ADDRESS_DUPLICATED(HttpStatus.BAD_REQUEST,"중복된 허브 이름입니다.")
    ,;

    private HttpStatus httpStatus;
    private String message;

    HubErrorCode(HttpStatus httpStatus, String message) {
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
}
