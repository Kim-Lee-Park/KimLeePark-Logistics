package com.klp.order.presentation.dto.order.response.cancel;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.presentation.dto.ordercancellation.response.OrderCancellationResponse;
import com.klp.order.presentation.dto.orderitem.response.OrderItemResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record CancelOrderResponse(
    UUID orderId,
    OrderStatus orderStatus,
    Long supplierId,
    Long customerId,
    List<OrderItemResponse> orderItems,
    OrderCancellationResponse cancellation,
    LocalDateTime createdAt,
    Long createdBy,
    LocalDateTime updatedAt,
    Long updatedBy
) {

    public static CancelOrderResponse from(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList());

        return new CancelOrderResponse(
            order.getOrderId(),
            order.getOrderStatus(),
            order.getSupplierId(),
            order.getCustomerId(),
            orderItemResponses,
            OrderCancellationResponse.from(order.getCancellation()),
            order.getCreatedAt(),
            order.getCreatedBy(),
            order.getUpdatedAt(),
            order.getUpdatedBy()
        );
    }
}







