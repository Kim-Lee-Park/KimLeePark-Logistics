package com.klp.order.product.applicaiton.service.dto;

import com.klp.order.payment.domain.event.PaymentCompletedEvent;
import java.util.List;
import java.util.UUID;

public record DeductStockCommand(
    List<Product> products
) {
    public static DeductStockCommand from(PaymentCompletedEvent event) {
        return new DeductStockCommand(
            event.paidInfos().stream().map(Product::from).toList()
        );
    }

    public record Product(
        UUID productId,
        Integer quantity
    ) {
        public static Product from(PaymentCompletedEvent.PaidInfo event) {
            return new Product(
                event.productId(),
                event.quantity()
            );
        }
    }
}
