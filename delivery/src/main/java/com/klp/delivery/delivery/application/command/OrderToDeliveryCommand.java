package com.klp.delivery.delivery.application.command;

import com.klp.delivery.delivery.domain.event.InventoryDeductedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderToDeliveryCommand(
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

    List<OrderItemCommand> products,

    String inventoryIdempotencyKey,
    String deliveryIdempotencyKey,

    LocalDateTime createdAt,
    LocalDateTime occurredAt,

    UUID paymentId,
    int paidAmount,
    String paymentMethod,
    LocalDateTime paidAt
) {

    public static OrderToDeliveryCommand from(InventoryDeductedEvent event) {
        List<OrderItemCommand> orderItems = event.products().stream()
            .map(OrderItemCommand::from)
            .toList();

        return new OrderToDeliveryCommand(
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
            orderItems,
            event.inventoryIdempotencyKey(),
            event.deliveryIdempotencyKey(),
            event.createdAt(),
            event.occurredAt(),
            event.paymentId(),
            event.paidAmount(),
            event.paymentMethod(),
            event.paidAt()
        );
    }

    public record OrderItemCommand(
        UUID orderItemId,
        UUID productId,
        String productName,
        UUID hubId,
        Integer quantity,
        int unitPrice,
        int totalPrice
    ) {

        public static OrderItemCommand from(InventoryDeductedEvent.OrderItem item) {
            return new OrderItemCommand(
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
}