package com.klp.order.presentation.dto.orderitem.response;

import com.klp.order.domain.entity.orderitem.OrderItem;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderItemResponse(
    UUID orderItemId,
    UUID productId,
    Integer quantity,
    UUID deliveryId,
    LocalDateTime createdAt,
    Long createdBy,
    LocalDateTime updatedAt,
    Long updatedBy,
    LocalDateTime deletedAt,
    Long deletedBy
) {

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
            orderItem.getOrderItemId(),
            orderItem.getProductId(),
            orderItem.getQuantity(),
            orderItem.getDeliveryId(),
            orderItem.getCreatedAt(),
            orderItem.getCreatedBy(),
            orderItem.getUpdatedAt(),
            orderItem.getUpdatedBy(),
            orderItem.getDeletedAt(),
            orderItem.getDeletedBy()
        );
    }
}
