package com.klp.payment.presentation.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
    UUID paymentId,
    UUID orderId,
    BigDecimal totalAmount
) {

}
