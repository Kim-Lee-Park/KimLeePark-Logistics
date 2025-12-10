package com.klp.order.order.domain.entity.cancel;

public enum CancelType {
    USER_REQUEST("고객 요청"),
    ADMIN_CANCEL("관리자 요청"),
    OUT_OF_STOCK("재고 소진");

    private String description;

    CancelType(String description) {
        this.description = description;
    }
}
