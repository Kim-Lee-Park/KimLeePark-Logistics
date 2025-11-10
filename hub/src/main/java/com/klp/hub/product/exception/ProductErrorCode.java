package com.klp.hub.product.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    NOT_FOUND_PRODUCT(HttpStatus.BAD_REQUEST, "해당 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
