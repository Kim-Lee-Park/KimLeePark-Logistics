package com.klp.payment.payment.presentation.dto.request;

import com.klp.payment.payment.application.command.FailPaymentCommand;
import jakarta.validation.constraints.NotBlank;

public record FailPaymentRequest(
    @NotBlank(message = "실패 사유는 필수입니다")
    String reason
) {

    public FailPaymentCommand toCommand() {
        return new FailPaymentCommand(reason);
    }
}
