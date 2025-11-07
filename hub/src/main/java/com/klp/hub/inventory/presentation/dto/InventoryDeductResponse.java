package com.klp.hub.inventory.presentation.dto;

public record InventoryDeductResponse(
    Process process
) {

    public static InventoryDeductResponse success() {
        return new InventoryDeductResponse(Process.SUCCESS);
    }

    public static InventoryDeductResponse already() {
        return new InventoryDeductResponse(Process.ALREADY_DEDUCTED);
    }

    public enum Process {
        SUCCESS, ALREADY_DEDUCTED
    }
}
