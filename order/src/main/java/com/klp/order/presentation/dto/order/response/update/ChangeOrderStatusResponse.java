package com.klp.order.presentation.dto.order.response.update;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import java.util.UUID;

public record ChangeOrderStatusResponse(
    UUID orderId,
    OrderStatus orderStatus
) {

    public static ChangeOrderStatusResponse from(Order order) {
        return new ChangeOrderStatusResponse(
            order.getOrderId(),
            order.getOrderStatus()
        );
    }
}
