package com.klp.order.presentation.dto.result;

import com.klp.order.domain.entity.order.Order;

public record OrderCreateWithKey(
    Order order,
    String inventoryIdempotencyKey,
    String deliveryIdempotencyKey
) {

}
