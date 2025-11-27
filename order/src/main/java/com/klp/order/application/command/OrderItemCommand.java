package com.klp.order.application.command;

import java.util.UUID;

public record OrderItemCommand(
    UUID productId,
    UUID hubId,
    int quantity
) {

}
