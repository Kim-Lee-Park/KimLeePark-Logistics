package com.klp.order.command;

import java.util.List;

public record CreateOrderCommand(
    Long supplierId,
    Long customerId,
    String comment,
    List<OrderItemCommand> items
) {

}
