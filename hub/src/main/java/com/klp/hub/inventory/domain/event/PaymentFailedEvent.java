package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 실패 시 재고 선점을 해제하기 위한 이벤트
 */
public record PaymentFailedEvent(
    UUID orderId,
    String idempotencyKey,
    String reason,
    LocalDateTime occurredAt
) {

}
