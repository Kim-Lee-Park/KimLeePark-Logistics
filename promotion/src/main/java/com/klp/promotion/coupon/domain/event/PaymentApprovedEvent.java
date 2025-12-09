package com.klp.promotion.coupon.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제 승인 이벤트 (구독용)
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
}

