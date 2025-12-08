package com.klp.hub.inventory.presentation.dto.response;

import java.util.UUID;

public record InventoryReservationResponse(
    boolean reserved,
    UUID orderId,
    String message
) {

    public static InventoryReservationResponse success(UUID orderId) {
        return new InventoryReservationResponse(true, orderId, "선점 성공");
    }

    public static InventoryReservationResponse already() {
        return new InventoryReservationResponse(true, null, "이미 처리된 요청");
    }

    public static InventoryReservationResponse failed(String reason) {
        return new InventoryReservationResponse(false, null, reason);
    }
}
