package com.klp.order.presentation.dto.order.request.update;

import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.command.UpdateOrderCommand;
import com.klp.order.presentation.dto.orderitem.request.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

public record UpdateOrderRequest(
    String comment,

    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> orderItems
) {

    public UpdateOrderCommand toCommand() {
        List<OrderItemCommand> itemCommands = orderItems.stream()
            .map(item -> new OrderItemCommand(item.productId(), item.quantity()))
            .collect(Collectors.toList());

        return new UpdateOrderCommand(comment, itemCommands);
    }
}
