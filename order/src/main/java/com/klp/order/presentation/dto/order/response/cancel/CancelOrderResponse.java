package com.klp.order.presentation.dto.order.response.cancel;

import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.domain.entity.cancel.CancelType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "주문 취소 응답")
public record CancelOrderResponse(
    @Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID orderId,

    @Schema(description = "취소 사유", example = "고객 변심")
    String cancelReason,

    @Schema(description = "취소 타입", example = "USER_REQUEST")
    CancelType cancelType,

    @Schema(description = "취소자 ID", example = "1")
    Long cancelledBy
) {

    public static CancelOrderResponse from(CancelOrderCommand command) {
        return new CancelOrderResponse(
            command.orderId(),
            command.cancelReason(),
            command.cancelType(),
            command.cancelledBy()
        );
    }
}