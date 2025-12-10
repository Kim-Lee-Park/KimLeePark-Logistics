package com.klp.order.order.infrastructure.client.dto.inventory.response;

public record DeductInventoryResponse(
    String status
) {

    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    public boolean isAlreadyDeducted() {
        return "ALREADY_DEDUCTED".equals(status);
    }
}
