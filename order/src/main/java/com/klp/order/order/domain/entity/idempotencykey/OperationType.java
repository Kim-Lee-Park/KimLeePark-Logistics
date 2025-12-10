package com.klp.order.order.domain.entity.idempotencykey;

public enum OperationType {
    INCREASE("증감"),
    DECREASE("감소"),
    MAKING("생성"),
    CANCEL("취소");

    private String description;

    private OperationType(String description) {
        this.description = description;
    }
}
