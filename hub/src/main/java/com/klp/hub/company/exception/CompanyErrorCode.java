package com.klp.hub.company.exception;

import com.klp.hub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {
    NOT_FOUND_COMPANY(HttpStatus.BAD_REQUEST, "해당 업체는 존재하지 않습니다."),
    UNSUPPORTED_COMPANY_TYPE(HttpStatus.BAD_REQUEST, "해당 업체 타입이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
