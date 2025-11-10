package com.klp.hub.inventory.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고 수량이 부족합니다."),
    NOT_FOUND_INVENTORY(HttpStatus.BAD_REQUEST, "재고를 찾을 수 없습니다."),
    INVENTORY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 해당 상품의 재고가 존재합니다");

    private final HttpStatus status;
    private final String message;
}
