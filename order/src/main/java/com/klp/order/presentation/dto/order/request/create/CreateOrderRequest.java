package com.klp.order.presentation.dto.order.request.create;

import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.presentation.dto.orderitem.request.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    @NotNull(message = "사용자 ID는 필수입니다.")
    Long userId,

    @NotNull(message = "공급 업체 ID는 필수입니다.")
    UUID supplierId,

    UUID userCouponId,

    String comment,

    @NotBlank(message = "배송지 주소는 필수입니다.")
    String deliveryAddress,

    @NotNull(message = "배송지 위도는 필수입니다.")
    BigDecimal deliveryLatitude,

    @NotNull(message = "배송지 경도는 필수입니다.")
    BigDecimal deliveryLongitude,

    @NotNull(message = "주문 상품은 필수입니다.")
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> orderItems
) {

    public CreateOrderCommand toCommand() {
        List<OrderItemCommand> itemCommands = orderItems.stream()
            .map(item -> new OrderItemCommand(
                item.productId(),
                item.productName(),
                item.hubId(),
                item.quantity(),
                item.price()
            ))
            .toList();

        return new CreateOrderCommand(
            userId,
            supplierId,
            userCouponId,
            comment,
            deliveryAddress,
            deliveryLatitude,
            deliveryLongitude,
            itemCommands
        );
    }
}







