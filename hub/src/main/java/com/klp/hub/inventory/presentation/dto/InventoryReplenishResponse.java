package com.klp.hub.inventory.presentation.dto;

public record InventoryReplenishResponse(
    Status status
) {

    public static InventoryReplenishResponse success() {
        return new InventoryReplenishResponse(Status.SUCCESS);
    }

    public static InventoryReplenishResponse already() {
        return new InventoryReplenishResponse(Status.ALREADY_REPLENISHED);
    }

    public enum Status {
        SUCCESS, ALREADY_REPLENISHED
    }
}
