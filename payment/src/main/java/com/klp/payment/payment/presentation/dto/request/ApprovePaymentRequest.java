package com.klp.payment.payment.presentation.dto.request;

import com.klp.payment.payment.application.command.ApprovePaymentCommand;
import com.klp.payment.payment.domain.enums.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApprovePaymentRequest(
    @NotBlank(message = "PG사 거래 ID는 필수입니다")
    String pgTransactionId,
    String billingKey,
    @NotNull(message = "카드 종류는 필수입니다")
    CardType cardType,
    Integer installmentMonths
) {

    public ApprovePaymentCommand toCommand() {
        return new ApprovePaymentCommand(pgTransactionId, billingKey, cardType, installmentMonths);
    }
}
