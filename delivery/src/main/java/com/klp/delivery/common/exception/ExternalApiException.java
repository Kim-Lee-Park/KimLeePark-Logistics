package com.klp.delivery.common.exception;

import com.klp.delivery.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ExternalApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
