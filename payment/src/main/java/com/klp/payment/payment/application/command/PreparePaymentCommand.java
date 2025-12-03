package com.klp.payment.payment.application.command;

import java.util.UUID;

public record PreparePaymentCommand(
    UUID orderId,
    Long amount
) {

}
