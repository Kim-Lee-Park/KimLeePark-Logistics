package com.klp.order.order.presentation.dto.order.response.update;

import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.domain.entity.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "주문 상태 변경 응답")
public record ChangeOrderStatusResponse(
    @Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID orderId,

    @Schema(description = "변경된 주문 상태", example = "PAID")
    OrderStatus orderStatus
) {

    public static ChangeOrderStatusResponse from(Order order) {
        return new ChangeOrderStatusResponse(
            order.getOrderId(),
            order.getOrderStatus()
        );
    }
}