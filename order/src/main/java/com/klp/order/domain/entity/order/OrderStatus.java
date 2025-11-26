package com.klp.order.domain.entity.order;

public enum OrderStatus {
    PENDING("진행 중"),
    DELIVERY_ASSIGNED("배송 할당"),
    COMPLETE("완료"),
    CANCELLED("취소됨"),
    FAILED("실패");

    private String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
