package com.klp.payment.application.service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentCreateCommand(
    UUID orderId,
    List<PaymentInfo> infos
) {

    public record PaymentInfo(
        UUID productId,
        Integer quantity,
        BigDecimal amount
    ) {
        public BigDecimal totalAmount() {
            return amount.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal totalAmount() {
        return infos().stream()
            .map(PaymentInfo::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
