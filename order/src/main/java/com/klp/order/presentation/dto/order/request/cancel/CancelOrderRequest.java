package com.klp.order.presentation.dto.order.request.cancel;

import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.domain.entity.cancel.CancelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CancelOrderRequest(
    @NotBlank(message = "취소 사유는 필수입니다.")
    String cancelReason,

    @NotNull(message = "취소 타입은 필수입니다.")
    CancelType cancelType
) {

    public CancelOrderCommand toCommand(Long cancelledBy) {
        return new CancelOrderCommand(
            cancelReason,
            cancelledBy,
            cancelType
        );
    }
}
