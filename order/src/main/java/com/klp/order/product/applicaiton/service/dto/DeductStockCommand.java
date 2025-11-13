package com.klp.order.product.applicaiton.service.dto;

import java.util.List;
import java.util.UUID;

public record DeductStockCommand(
    List<Product> products
) {
    public record Product(
        UUID productId,
        Integer quantity
    ) {

    }
}
