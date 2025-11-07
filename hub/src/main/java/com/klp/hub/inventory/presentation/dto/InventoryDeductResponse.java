package com.klp.hub.inventory.presentation.dto;

public record InventoryDeductResponse(
    Status status
) {

    public static InventoryDeductResponse success() {
        return new InventoryDeductResponse(Status.SUCCESS);
    }

    public static InventoryDeductResponse already() {
        return new InventoryDeductResponse(Status.ALREADY_DEDUCTED);
    }

    public enum Status {
        SUCCESS, ALREADY_DEDUCTED
    }
}
