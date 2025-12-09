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

    public static OrderCreatedEvent from(
        Order order,
        String email,
        String address,
        String inventoryIdempotencyKey,
        String deliveryIdempotencyKey,
        UUID userAddressHubId
    ) {
        List<OrderItem> orderItems = order.getOrderItems().stream()
            .map(item -> new OrderItem(
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
            email,
            order.getOriginalPrice(),
            order.getCouponDiscountPrice(),
            order.getGradeDiscountPrice(),
            order.getOrderPrice(),
            order.getAddressId(),
            userAddressHubId,
            address,
            order.getDeliveryLatitude(),
            order.getDeliveryLongitude(),
            orderItems,
            inventoryIdempotencyKey,
            deliveryIdempotencyKey,
            order.getCreatedAt(),
            LocalDateTime.now()
        );
    }
}