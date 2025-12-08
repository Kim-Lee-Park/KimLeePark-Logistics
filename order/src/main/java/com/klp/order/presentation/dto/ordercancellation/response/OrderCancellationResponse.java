package com.klp.order.presentation.dto.ordercancellation.response;

import com.klp.order.domain.entity.cancel.CancelType;
import com.klp.order.domain.entity.cancel.OrderCancellation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 취소 정보")
public record OrderCancellationResponse(
    @Schema(description = "취소 ID", example = "bb0e8400-e29b-41d4-a716-446655440000")
    UUID orderCancellationId,

    @Schema(description = "취소 사유", example = "고객 변심")
    String cancelReason,

    @Schema(description = "취소자 ID", example = "1")
    Long cancelledBy,

    @Schema(description = "취소일시", example = "2024-01-01T10:00:00")
    LocalDateTime cancelledAt,

    @Schema(description = "취소 타입", example = "USER_REQUEST")
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