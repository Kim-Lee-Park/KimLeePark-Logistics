package com.klp.order.domain.entity.orderitem;

import java.util.UUID;

public record OrderItemCommand(
    UUID productId,
    int quantity
) {

}
