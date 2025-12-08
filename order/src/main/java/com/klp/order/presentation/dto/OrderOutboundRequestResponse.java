package com.klp.order.presentation.dto;

import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.entity.idempotencykey.Target;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "주문 외부 요청 응답")
public record OrderOutboundRequestResponse(
    @Schema(description = "요청 ID", example = "UUID")
    UUID requestId,

    @Schema(description = "주문 ID", example = "UUID")
    UUID orderId,

    @Schema(description = "멱등성 키", example = "550e8400-INVENTORY-DECREASE-1704067200000")
    String idempotencyKey,

    @Schema(
        description = "요청 대상",
        example = "DELIVERY",
        allowableValues = {"DELIVERY", "INVENTORY"}
    )
    Target target,

    @Schema(
        description = "작업 타입",
        example = "DECREASE",
        allowableValues = {"INCREASE", "DECREASE", "MAKING", "CANCEL"}
    )
    OperationType operation
) {

    public static OrderOutboundRequestResponse from(OrderOutboundRequest entity) {
        return new OrderOutboundRequestResponse(
            entity.getRequestId(),
            entity.getOrder().getOrderId(),
            entity.getIdempotencyKey(),
            entity.getTarget(),
            entity.getOperation()
        );
    }
}