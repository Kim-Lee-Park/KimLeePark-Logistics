package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 쿠폰 사용 확정 후 재고 차감을 위한 이벤트
 */
public record CouponUsedEvent(
    UUID orderId,
    String idempotencyKey,
    List<OrderItemDto> items,
    LocalDateTime occurredAt
) {

    public record OrderItemDto(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }
}
