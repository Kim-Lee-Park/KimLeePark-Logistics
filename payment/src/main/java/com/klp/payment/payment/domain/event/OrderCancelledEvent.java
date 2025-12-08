package com.klp.payment.payment.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order 서비스에서 발행하는 주문 취소 이벤트 (구독용)
 */
public record OrderCancelledEvent(
    UUID orderId,
    Long userId,
    String reason,
    LocalDateTime occurredAt
) {
}
