package com.klp.order.order.domain.entity.outbox;

public enum OrderOutboxStatus {
    PENDING,
    PUBLISHING,
    PUBLISHED,
    FAILED
}
