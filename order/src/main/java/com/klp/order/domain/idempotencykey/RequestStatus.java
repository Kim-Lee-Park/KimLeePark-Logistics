package com.klp.order.domain.idempotencykey;

public enum RequestStatus {
    PENDING("사용 전"),
    DONE("사용 완료");

    private String description;

    RequestStatus(String description) {
        this.description = description;
    }
}
