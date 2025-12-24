package com.klp.delivery.common.enums;

public enum RoutePlanStatus {
    ACTIVE,
    PENDING_DELETE,
    DELETED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isPendingDelete() {
        return this == PENDING_DELETE;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
