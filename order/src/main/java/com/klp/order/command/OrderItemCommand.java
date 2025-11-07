package com.klp.order.command;

import java.util.UUID;

public record OrderItemCommand(
    UUID productId,
    int quantity
) {

}
