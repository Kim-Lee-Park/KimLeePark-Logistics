package com.klp.order.infrastructure.event.event;

import com.klp.order.domain.entity.order.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    public static OrderCreatedEvent from(
        Order order,
        String inventoryIdempotencyKey,
        String deliveryIdempotencyKey
    ) {
        List<ProductDeduction> products = order.getOrderItems().stream()
            .map(item -> new ProductDeduction(
                item.getOrderItemId(),
                item.getProductId(),
                item.getProductName(),
                item.getHubId(),
                item.getQuantity(),
                item.getPrice(),
                item.getTotalPrice()
            ))
            .toList();

        return new OrderCreatedEvent(
            order.getOrderId(),
            order.getUserId(),
            order.getSupplierId(),
            order.getUserCouponId(),
            order.getOriginalPrice(),
            order.getCouponDiscountPrice(),
            order.getGradeDiscountPrice(),
            order.getOrderPrice(),
            order.getDeliveryAddress(),
            order.getDeliveryLatitude(),
            order.getDeliveryLongitude(),
            products,
            inventoryIdempotencyKey,
            deliveryIdempotencyKey,
            LocalDateTime.now()
        );
    }
}