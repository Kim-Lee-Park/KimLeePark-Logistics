package com.klp.order.presentation.dto.order.response.get;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.presentation.dto.ordercancellation.response.OrderCancellationResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public record GetOrdersResponse(
    UUID orderId,
    UUID supplierId,
    Long userId,
    UUID userCouponId,
    OrderStatus orderStatus,
    int originalPrice,
    int orderPrice,
    OrderCancellationResponse cancellation,
    Long createdBy,
    LocalDateTime createdAt
) {

    public static GetOrdersResponse from(Order order) {
        return new GetOrdersResponse(
            order.getOrderId(),
            order.getSupplierId(),
            order.getUserId(),
            order.getUserCouponId(),
            order.getOrderStatus(),
            order.getOriginalPrice(),
            order.getOrderPrice(),
            OrderCancellationResponse.from(order.getCancellation()),
            order.getCreatedBy(),
            order.getCreatedAt()
        );
    }
}