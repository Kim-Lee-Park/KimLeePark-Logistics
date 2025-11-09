package com.klp.order.command;

import java.util.List;

public record UpdateOrderCommand(
    String comment,
    List<OrderItemCommand> items
) {

}
