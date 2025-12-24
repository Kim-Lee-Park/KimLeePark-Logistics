package com.klp.payment.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentApprovedEvent(
    // Payment 고유 데이터
    UUID paymentId,
    int paidAmount,
    String paymentMethod,
    LocalDateTime paidAt,

    // Order에서 받은 데이터
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

    public static PaymentApprovedEvent from(
        UUID paymentId,
        int paidAmount,
        String paymentMethod,
        LocalDateTime paidAt,
        OrderCreatedEvent orderEvent
    ) {
        List<OrderItem> products = orderEvent.products().stream()
            .map(p -> new OrderItem(
                p.orderItemId(),
                p.productId(),
                p.productName(),
                p.hubId(),
                p.quantity(),
                p.unitPrice(),
                p.totalPrice()
            ))
            .toList();

        return new PaymentApprovedEvent(
            paymentId,
            paidAmount,
            paymentMethod,
            paidAt,
            orderEvent.orderId(),
            orderEvent.userId(),
            orderEvent.supplierId(),
            orderEvent.userCouponId(),
            orderEvent.email(),
            orderEvent.username(),
            orderEvent.comment(),
            orderEvent.originalPrice(),
            orderEvent.couponDiscountPrice(),
            orderEvent.gradeDiscountPrice(),
            orderEvent.finalOrderPrice(),
            orderEvent.addressId(),
            orderEvent.userAddressHubId(),
            orderEvent.address(),
            orderEvent.deliveryLatitude(),
            orderEvent.deliveryLongitude(),
            products,
            orderEvent.inventoryIdempotencyKey(),
            orderEvent.deliveryIdempotencyKey(),
            orderEvent.createdAt(),
            LocalDateTime.now()
        );
    }
}