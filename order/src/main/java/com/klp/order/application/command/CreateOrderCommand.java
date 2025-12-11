package com.klp.order.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
    Long userId,
    UUID supplierId,
    UUID userCouponId,
    String comment,
    UUID addressId,
    BigDecimal deliveryLatitude,
    BigDecimal deliveryLongitude,
    List<OrderItemCommand> items
) {

}
