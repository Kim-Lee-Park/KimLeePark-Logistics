package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제 취소 시 재고 복구를 위한 이벤
 */
public record PaymentCancelledEvent(
    UUID orderId,
    String idempotencyKey,
    List<CancelledItemDto> items,
    LocalDateTime occurredAt
) {

    public record CancelledItemDto(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }
}
