package com.klp.order.presentation.dto.order.request.update;

import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.command.UpdateOrderCommand;
import com.klp.order.presentation.dto.orderitem.request.OrderItemRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "주문 수정 요청")
public record UpdateOrderRequest(

    @Schema(description = "주문 코멘트", example = "배송 전 연락 부탁드립니다.")
    String comment,

    @Schema(description = "수정할 주문 상품 목록", required = true)
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> orderItems
) {

    public UpdateOrderCommand toCommand() {
        List<OrderItemCommand> itemCommands = orderItems.stream()
            .map(item -> new OrderItemCommand(item.productId(), item.productName(), item.hubId(),
                item.price(),
                item.quantity()))
            .toList();

        return new UpdateOrderCommand(comment, itemCommands);
    }
}
