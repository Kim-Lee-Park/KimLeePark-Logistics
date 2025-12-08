package com.klp.payment.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제 승인 이벤트 (발행용)
 */
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

    int originalPrice,
    int couponDiscountPrice,
    int gradeDiscountPrice,

    String deliveryAddress,
    BigDecimal deliveryLatitude,
    BigDecimal deliveryLongitude,

    List<ProductInfo> products,

    String deliveryIdempotencyKey,
    String couponIdempotencyKey,

    LocalDateTime occurredAt
) {

    public record ProductInfo(
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
        OrderCreatedEvent orderEvent,
        String couponIdempotencyKey
    ) {
        List<ProductInfo> products = orderEvent.products().stream()
            .map(p -> new ProductInfo(
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
            orderEvent.originalPrice(),
            orderEvent.couponDiscountPrice(),
            orderEvent.gradeDiscountPrice(),
            orderEvent.deliveryAddress(),
            orderEvent.deliveryLatitude(),
            orderEvent.deliveryLongitude(),
            products,
            orderEvent.deliveryIdempotencyKey(),
            couponIdempotencyKey,
            LocalDateTime.now()
        );
    }
}
