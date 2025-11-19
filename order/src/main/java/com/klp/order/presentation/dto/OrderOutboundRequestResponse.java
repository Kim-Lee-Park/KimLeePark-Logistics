package com.klp.order.presentation.dto;

import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.entity.idempotencykey.Target;
import java.util.UUID;

public record OrderOutboundRequestResponse(
    UUID requestId,
    UUID orderId,
    String idempotencyKey,
    Target target,
    OperationType operation
) {

    public static OrderOutboundRequestResponse from(OrderOutboundRequest entity) {
        return new OrderOutboundRequestResponse(
            entity.getReqeustId(),
            entity.getOrder().getOrderId(),
            entity.getIdempotencyKey(),
            entity.getTarget(),
            entity.getOperation()
        );
    }
}
