package com.klp.order.application.command;

import java.util.UUID;

public record OrderItemCommand(
    UUID productId,
    String productName,
    UUID hubId,
    int quantity,
    int price
) {

    public int getTotalPrice() {
        return quantity * price;
    }
}
