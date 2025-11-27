package com.klp.order.global.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderItemErrorCode implements ErrorCode {

    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 아이템을 찾을 수 없습니다."),
    ORDER_REQUIRED(HttpStatus.BAD_REQUEST, "주문은 필수입니다."),
    PRODUCT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "상품 ID는 필수입니다."),
    HUB_ID_REQUIRED(HttpStatus.BAD_REQUEST, "허브 ID는 필수입니다."),
    QUANTITY_MIN_REQUIRED(HttpStatus.BAD_REQUEST, "주문 수량은 1개 이상이어야 합니다."),
    ORDER_ITEMS_EMPTY(HttpStatus.BAD_REQUEST, "주문 아이템이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
