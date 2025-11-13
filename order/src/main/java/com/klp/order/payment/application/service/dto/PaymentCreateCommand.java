package com.klp.order.payment.application.service.dto;

import com.klp.order.order.domain.event.OrderCreatedEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentCreateCommand(
    UUID orderId,
    List<PaymentInfo> infos
) {
    public static PaymentCreateCommand from(OrderCreatedEvent event) {
        return new PaymentCreateCommand(
            event.orderId(),
            event.products().stream().map(PaymentInfo::from).toList()
        );
    }

    public record PaymentInfo(
        UUID productId,
        Integer quantity,
        BigDecimal amount
    ) {
        public static PaymentInfo from(OrderCreatedEvent.Product product) {
            return new PaymentInfo(
                product.productId(),
                product.quantity(),
                BigDecimal.valueOf(product.price())
            );
        }

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
