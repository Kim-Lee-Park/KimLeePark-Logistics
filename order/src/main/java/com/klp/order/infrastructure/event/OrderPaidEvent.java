package com.klp.order.infrastructure.event;

import com.klp.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderPaidEvent(
    UUID orderId,
    String idempotencyKey,
    Long supplierId,
    Long customerId,
    List<DeliveryItem> items,
    LocalDateTime occurredAt
) {

    public record DeliveryItem(
        UUID orderItemId,
        UUID hubId,
        String productName,
        Integer quantity
    ) {

    }

    public static OrderPaidEvent from(Order order, String idempotencyKey) {
        List<DeliveryItem> items = order.getOrderItems().stream()
            .map(item -> new DeliveryItem(
                item.getOrderItemId(),
                item.getHubId(),
                item.getProductName(),
                item.getQuantity()
            ))
            .toList();

        return new OrderPaidEvent(
            order.getOrderId(),
            idempotencyKey,
            order.getSupplierId(),
            order.getCustomerId(),
            items,
            LocalDateTime.now()
        );
    }
}
