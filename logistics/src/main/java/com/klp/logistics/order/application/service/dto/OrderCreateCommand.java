package com.klp.logistics.order.application.service.dto;

import com.klp.logistics.order.domain.entity.orderitem.OrderItemCommand;
import java.util.List;
import java.util.UUID;

public record OrderCreateCommand(
    Long supplierId,
    Long customerId,
    List<Product> products,
    String comments
) {
    public List<OrderItemCommand> toOrderItemCommands() {
        return products().stream().map(
            Product::toOrderItemCommand
        ).toList();
    }
    public record Product(
        UUID productId,
        Integer quantity,
        Integer price
    ) {
        public OrderItemCommand toOrderItemCommand() {
            return new OrderItemCommand(
                productId,
                quantity
            );
        }
    }
}
