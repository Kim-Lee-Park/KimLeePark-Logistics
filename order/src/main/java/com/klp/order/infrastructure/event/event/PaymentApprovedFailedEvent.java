package com.klp.order.infrastructure.event.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentApprovedFailedEvent(
    UUID orderId,
    Long userId,
    UUID supplierId,
    UUID userCouponId,
    String email,
    String username,
    String comment,

    int originalPrice,
    int couponDiscountPrice,
    int gradeDiscountPrice,
    int finalOrderPrice,

    UUID addressId,
    UUID userAddressHubId,
    String address,
    BigDecimal deliveryLatitude,
    BigDecimal deliveryLongitude,

    List<OrderCreatedEvent.OrderItem> products,

    String inventoryIdempotencyKey,
    String deliveryIdempotencyKey,

    LocalDateTime createdAt,
    LocalDateTime occurredAt
) {

    public record OrderItem(
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
