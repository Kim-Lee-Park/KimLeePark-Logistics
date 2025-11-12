package com.klp.order.order.domain.entity.orderitem;

import java.util.UUID;

public record OrderItemCommand(
    UUID productId,
    int quantity
) {

}
