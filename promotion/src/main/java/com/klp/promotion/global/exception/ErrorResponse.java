package com.klp.promotion.global.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final String errorName;
    private final String message;

    public ErrorResponse(String errorName, String message) {
        this.errorName = errorName;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message);
    }
}
