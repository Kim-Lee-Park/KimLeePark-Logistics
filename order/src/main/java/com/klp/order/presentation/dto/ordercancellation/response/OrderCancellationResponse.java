package com.klp.order.presentation.dto.ordercancellation.response;

import com.klp.order.domain.entity.cancel.CancelType;
import com.klp.order.domain.entity.cancel.OrderCancellation;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCancellationResponse(
    UUID orderCancellationId,
    String cancelReason,
    Long cancelledBy,
    LocalDateTime cancelledAt,
    CancelType cancelType
) {

    public static OrderCancellationResponse from(OrderCancellation cancellation) {
        if (cancellation == null) {
            return null;
        }

        return new OrderCancellationResponse(
            cancellation.getOrderCancellationId(),
            cancellation.getCancelReason(),
            cancellation.getCancelledBy(),
            cancellation.getCancelledAt(),
            cancellation.getCancelType()
        );
    }
}
