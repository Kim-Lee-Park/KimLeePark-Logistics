package com.klp.hub.company.exception;

import com.klp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {
    NOT_FOUND_COMPANY(HttpStatus.BAD_REQUEST, "해당 업체는 존재하지 않습니다.");

    private final HttpStatus status;

    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
