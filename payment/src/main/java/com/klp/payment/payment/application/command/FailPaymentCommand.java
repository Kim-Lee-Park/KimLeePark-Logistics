package com.klp.payment.payment.application.command;

public record FailPaymentCommand(
    String reason
) {

}
