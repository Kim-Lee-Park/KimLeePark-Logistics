package com.klp.order.application.command;

import java.util.List;

public record UpdateOrderCommand(
    String comment,
    List<OrderItemCommand> items
) {

}
