package com.klp.hub.inventory.domain;

import lombok.Getter;

@Getter
public enum InventoryReservationStatus {
    RESERVED,
    CONFIRMED,
    RELEASED
}
