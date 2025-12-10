package com.klp.delivery.delivery.domain.event;

import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryCreatedFailedEvent(
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

        // OrderItemCommand에서 OrderItem 생성
        public static OrderItem from(OrderItemCommand item) {
            return new OrderItem(
                item.orderItemId(),
                item.productId(),
                item.productName(),
                item.hubId(),
                item.quantity(),
                item.unitPrice(),
                item.totalPrice()
            );
        }
    }

    // OrderToDeliveryCommand에서 DeliveryCreatedFailedEvent 생성
    public static DeliveryCreatedFailedEvent from(OrderToDeliveryCommand command) {
        List<OrderItem> failedItems = command.products().stream()
            .map(OrderItem::from)
            .toList();

        return new DeliveryCreatedFailedEvent(
            command.orderId(),
            command.userId(),
            command.supplierId(),
            command.userCouponId(),
            command.email(),
            command.username(),
            command.comment(),
            command.originalPrice(),
            command.couponDiscountPrice(),
            command.gradeDiscountPrice(),
            command.finalOrderPrice(),
            command.addressId(),
            command.userAddressHubId(),
            command.address(),
            command.deliveryLatitude(),
            command.deliveryLongitude(),
            failedItems,
            command.inventoryIdempotencyKey(),
            command.deliveryIdempotencyKey(),
            command.createdAt(),
            LocalDateTime.now()
        );
    }
}