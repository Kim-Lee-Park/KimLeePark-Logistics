package com.klp.payment.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CouponUsedFailedEvent(
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

    List<OrderItem> products,

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
    public static CouponUsedFailedEvent from(
        PaymentApprovedEvent event
    ) {
        List<OrderItem> products = event.products().stream()
            .map(p -> new OrderItem(
                p.productId(),
                p.hubId(),
                p.productName(),
                p.hubId(),
                p.quantity(),
                p.unitPrice(),
                p.totalPrice()
            ))
            .toList();

        return new CouponUsedFailedEvent(
            event.orderId(),
            event.userId(),
            event.supplierId(),
            event.userCouponId(),
            event.email(),
            event.username(),
            event.comment(),
            event.originalPrice(),
            event.couponDiscountPrice(),
            event.gradeDiscountPrice(),
            event.finalOrderPrice(),
            event.addressId(),
            event.userAddressHubId(),
            event.address(),
            event.deliveryLatitude(),
            event.deliveryLongitude(),
            products,
            event.inventoryIdempotencyKey(),
            event.deliveryIdempotencyKey(),
            LocalDateTime.now(),
            event.occurredAt()
        );
    }
}

