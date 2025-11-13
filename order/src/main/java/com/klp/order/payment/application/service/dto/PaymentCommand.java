package com.klp.order.payment.application.service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCommand(
    UUID orderId,
    BigDecimal amount
) {
}
