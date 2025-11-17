package com.klp.order.presentation.dto.order.response.delete;

import com.klp.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeleteOrderResponse(
    String message,
    UUID orderId,
    Long deletedBy,
    LocalDateTime deletedAt
) {

    public static DeleteOrderResponse from(Order order) {
        return new DeleteOrderResponse(
            "주문이 성공적으로 삭제되었습니다.",
            order.getOrderId(),
            order.getDeletedBy(),
            order.getDeletedAt()
        );
    }
}
