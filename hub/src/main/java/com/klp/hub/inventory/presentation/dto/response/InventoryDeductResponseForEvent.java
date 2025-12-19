package com.klp.hub.inventory.presentation.dto.response;

public record InventoryDeductResponseForEvent(
    Status status,
    String message
) {

    public static InventoryDeductResponseForEvent success() {
        return new InventoryDeductResponseForEvent(Status.SUCCESS, "재고 차감 성공");
    }

    public static InventoryDeductResponseForEvent already() {
        return new InventoryDeductResponseForEvent(Status.ALREADY_DEDUCTED, "이미 처리된 요청");
    }

    public static InventoryDeductResponseForEvent failed(String reason) {
        return new InventoryDeductResponseForEvent(Status.FAILED, reason);
    }

    public enum Status {
        SUCCESS,
        ALREADY_DEDUCTED,
        FAILED
    }
}
