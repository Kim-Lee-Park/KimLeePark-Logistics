package com.klp.payment.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order 서비스에서 발행하는 주문 생성 이벤트 (구독용)
 */
public record OrderCreatedEvent(
    UUID orderId,
    Long userId,
    UUID supplierId,
    UUID userCouponId,

    int originalPrice,
    int couponDiscountPrice,
    int gradeDiscountPrice,
    int finalOrderPrice,

    String deliveryAddress,
    BigDecimal deliveryLatitude,
    BigDecimal deliveryLongitude,

    List<ProductDeduction> products,

    String inventoryIdempotencyKey,
    String deliveryIdempotencyKey,

    LocalDateTime occurredAt
) {

    public record ProductDeduction(
        UUID orderItemId,
        UUID productId,
        String productName,
        UUID hubId,
        Integer quantity,
        int unitPrice,
        int totalPrice
    ) {
    }
}
