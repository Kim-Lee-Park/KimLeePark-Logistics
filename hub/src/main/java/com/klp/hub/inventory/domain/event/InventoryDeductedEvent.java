package com.klp.hub.inventory.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryDeductedEvent(
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
    LocalDateTime occurredAt,

    UUID paymentId,
    int paidAmount,
    String paymentMethod,
    LocalDateTime paidAt
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

    public static InventoryDeductedEvent of(CouponUsedEvent event) {
        return new InventoryDeductedEvent(
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
            event.products().stream()
                .map(product -> new OrderItem(
                    product.orderItemId(),
                    product.productId(),
                    product.productName(),
                    product.hubId(),
                    product.quantity(),
                    product.unitPrice(),
                    product.totalPrice()
                ))
                .toList(),
            event.inventoryIdempotencyKey(),
            event.deliveryIdempotencyKey(),
            event.createdAt(),
            LocalDateTime.now(),
            event.paymentId(),
            event.paidAmount(),
            event.paymentMethod(),
            event.paidAt()
        );
    }
}
