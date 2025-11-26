package com.klp.hub.inventory.domain;

public enum InventoryIdempotencyStatus {
    IN_PROGRESS,
    SUCCESS;

    public boolean isUsed() {
        return this == SUCCESS;
    }
}
