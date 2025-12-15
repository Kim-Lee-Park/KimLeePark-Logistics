package com.klp.order.infrastructure.client.dto.inventory.response;

import java.util.UUID;

public record InventoryReservationResponse(
    boolean reserved,
    UUID orderId,
    String message
) {
    // 성공 ( true, orderId, "선점 성공")
    // 이미 존재 (true,null,"이미 처리된 요청")
    // 실패 (false,null,reason)
}
