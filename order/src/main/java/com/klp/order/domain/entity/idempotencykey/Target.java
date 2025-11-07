package com.klp.order.domain.entity.idempotencykey;

public enum Target {
    DELIVERY("배송"),
    INVENTORY("재고");

    private String description;

    Target(String description) {
        this.description = description;
    }
}
