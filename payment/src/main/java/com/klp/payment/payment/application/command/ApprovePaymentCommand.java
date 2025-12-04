package com.klp.payment.payment.application.command;

import com.klp.payment.payment.domain.enums.CardType;

public record ApprovePaymentCommand(
    String pgTransactionId,
    String billingKey,
    CardType cardType,
    Integer installmentMonths
) {

}
