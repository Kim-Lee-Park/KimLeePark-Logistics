package com.klp.payment.payment.presentation.dto.request;

import com.klp.payment.payment.application.command.PreparePaymentCommand;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PreparePaymentRequest(
    @NotNull(message = "주문 ID는 필수입니다")
    UUID orderId,
    UUID hubId,
    @NotNull(message = "결제 금액은 필수입니다")
    Long amount
) {

    public PreparePaymentCommand toCommand(Long userId) {
        return new PreparePaymentCommand(orderId, userId, hubId, amount);
    }
}
