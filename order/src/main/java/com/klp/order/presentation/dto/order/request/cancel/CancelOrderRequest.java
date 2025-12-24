package com.klp.order.presentation.dto.order.request.cancel;

import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.domain.entity.cancel.CancelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "주문 취소 요청")
public record CancelOrderRequest(
    @Schema(
        description = "취소 사유",
        example = "고객 변심",
        required = true
    )
    @NotBlank(message = "취소 사유는 필수입니다.")
    String cancelReason,

    @Schema(
        description = "취소 타입",
        example = "USER_REQUEST",
        required = true,
        allowableValues = {"USER_REQUEST", "ADMIN_CANCEL", "OUT_OF_STOCK"}
    )
    @NotNull(message = "취소 타입은 필수입니다.")
    CancelType cancelType
) {

    public CancelOrderCommand toCommand(UUID orderId, Long cancelledBy) {
        return new CancelOrderCommand(
            orderId,
            null,
            cancelReason,
            cancelledBy,
            cancelType,
            null
        );
    }
}
