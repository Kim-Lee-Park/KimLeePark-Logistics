package com.klp.delivery.delivery.domain.event;

import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryCreatedEvent(
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

    public static DeliveryCreatedEvent from(OrderToDeliveryCommand orderCommand,
        List<OrderItem> allEventItems) {
        return new DeliveryCreatedEvent(    orderCommand.orderId(),
            orderCommand.userId(),
            orderCommand.supplierId(),
            orderCommand.userCouponId(),
            orderCommand.email(),
            orderCommand.username(),
            orderCommand.comment(),
            orderCommand.originalPrice(),
            orderCommand.couponDiscountPrice(),
            orderCommand.gradeDiscountPrice(),
            orderCommand.finalOrderPrice(),
            orderCommand.addressId(),
            orderCommand.userAddressHubId(),
            orderCommand.address(),
            orderCommand.deliveryLatitude(),
            orderCommand.deliveryLongitude(),
            allEventItems,
            orderCommand.inventoryIdempotencyKey(),
            orderCommand.deliveryIdempotencyKey(),
            orderCommand.createdAt(),
            orderCommand.occurredAt()
        );
    }

    public record OrderItem(
        UUID orderItemId,
        UUID productId,
        String productName,
        UUID hubId,
        Integer quantity,
        int unitPrice,
        int totalPrice,
        UUID deliveryId
    ) {

    }
}