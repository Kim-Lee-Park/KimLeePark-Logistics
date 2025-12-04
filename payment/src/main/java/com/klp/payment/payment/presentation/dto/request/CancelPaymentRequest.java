package com.klp.payment.payment.presentation.dto.request;

import com.klp.payment.payment.application.command.CancelPaymentCommand;
import jakarta.validation.constraints.NotBlank;

public record CancelPaymentRequest(
    @NotBlank(message = "취소 사유는 필수입니다")
    String reason
) {

    public CancelPaymentCommand toCommand() {
        return new CancelPaymentCommand(reason);
    }
}
