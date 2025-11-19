package com.klp.order.presentation.dto.order.request.create;

import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.presentation.dto.orderitem.request.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public record CreateOrderRequest(
    @NotNull(message = "공급 업체 ID는 필수입니다.")
    Long supplierId,

    @NotNull(message = "수령 업체 ID는 필수입니다.")
    Long customerId,

    String comment,

    @NotNull(message = "주문 상품은 필수입니다.")
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> orderItems
) {

    public CreateOrderCommand toCommand() {
        List<OrderItemCommand> itemCommands = orderItems.stream()
            .map(item -> new OrderItemCommand(item.productId(), item.quantity()))
            .collect(Collectors.toList());

        return new CreateOrderCommand(
            supplierId,
            customerId,
            comment,
            itemCommands
        );
    }
}







