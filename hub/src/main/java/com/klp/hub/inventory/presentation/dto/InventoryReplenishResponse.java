package com.klp.hub.inventory.presentation.dto;

public record InventoryReplenishResponse(
    Process process
) {

    public static InventoryReplenishResponse success() {
        return new InventoryReplenishResponse(Process.SUCCESS);
    }

    public static InventoryReplenishResponse already() {
        return new InventoryReplenishResponse(Process.ALREADY_REPLENISHED);
    }

    public enum Process {
        SUCCESS, ALREADY_REPLENISHED
    }
}
