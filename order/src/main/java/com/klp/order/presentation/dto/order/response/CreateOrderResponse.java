package com.klp.order.presentation.dto.order.response;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.presentation.dto.ordercancellation.response.OrderCancellationResponse;
import com.klp.order.presentation.dto.orderitem.response.OrderItemResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record CreateOrderResponse(
    UUID orderId,
    Long supplierId,
    Long customerId,
    String comment,
    OrderStatus orderStatus,
    List<OrderItemResponse> orderItems,
    OrderCancellationResponse cancellation,
    LocalDateTime createdAt,
    Long createdBy,
    LocalDateTime updatedAt,
    Long updatedBy,
    LocalDateTime deletedAt,
    Long deletedBy
) {

    public static CreateOrderResponse from(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList());

        return new CreateOrderResponse(
            order.getOrderId(),
            order.getSupplierId(),
            order.getCustomerId(),
            order.getComment(),
            order.getOrderStatus(),
            orderItemResponses,
            OrderCancellationResponse.from(order.getCancellation()),
            order.getCreatedAt(),
            order.getCreatedBy(),
            order.getUpdatedAt(),
            order.getUpdatedBy(),
            order.getDeletedAt(),
            order.getDeletedBy()
        );
    }
}
