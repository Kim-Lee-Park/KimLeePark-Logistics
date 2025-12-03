package com.klp.order.domain.entity.outbox;

public enum OrderOutboxStatus {
    PENDING,
    PUBLISHING,
    PUBLISHED,
    FAILED
}
