package com.klp.promotion.coupon.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 쿠폰 사용 확정 후 재고 차감을 위한 이벤트
 * InventoryDeductedFailedEvent의 기본 구조 + PaymentApprovedEvent의 추가 필드를 포함
 */
public record CouponUsedEvent(
    // InventoryDeductedFailedEvent 기본 구조 (order에서 발행)
    UUID orderId,
    Long userId,
    UUID supplierId,
    UUID userCouponId,
    String email,
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
    LocalDateTime occurredAt,

    // PaymentApprovedEvent 추가 필드
    UUID paymentId,
    int paidAmount,
    String paymentMethod,
    LocalDateTime paidAt,
    String couponIdempotencyKey
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

