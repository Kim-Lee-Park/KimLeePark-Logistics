package com.klp.order.application.command;

import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import java.util.UUID;

public record CreateOrderOutboundRequestCommand(
    UUID orderId,
    String idempotencyKey,
    Target target,
    OperationType operation
) {

}