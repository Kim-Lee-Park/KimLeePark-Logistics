package com.klp.order.domain.idempotencykey;

public enum OperationType {
    INCREASE("증감"),
    DECREASE("감소"),
    MAKING("생성");

    private String description;

    private OperationType(String description) {
        this.description = description;
    }
}
