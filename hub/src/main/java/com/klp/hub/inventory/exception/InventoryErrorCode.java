package com.klp.hub.inventory.exception;

import com.klp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고 수량이 부족합니다."),
    NOT_FOUND_INVENTORY(HttpStatus.BAD_REQUEST, "재고를 찾을 수 없습니다.");

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
