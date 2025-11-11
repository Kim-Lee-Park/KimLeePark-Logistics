package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record DeliveryCreateRequest(
    @NotNull(message = "orderId는 필수입니다.")
    String orderId,

    @NotBlank(message = "idempotencyKey는 필수입니다.")
    String idempotencykey,

    @NotNull(message = "supplierId는 필수입니다.")
    String supplierId,

    @NotNull(message = "customerId는 필수입니다.")
    String customerId,

    @NotEmpty(message = "orderItems는 필수이며 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItem> orderItems
) {

    public OrderToDeliveryCommand toOrderToDeliveryCommand() {
        return new OrderToDeliveryCommand(
            UUID.fromString(orderId),
            UUID.fromString(supplierId),
            UUID.fromString(customerId),
            orderItems.stream()
                .map(item -> new OrderItemCommand(
                    UUID.fromString(item.orderItemId()),
                    UUID.fromString(item.hubId())
                ))
                .toList()
        );
    }

    public IdempotencyCommand toIdempotencyCommand() {
        return new IdempotencyCommand(
            idempotencykey,
            UUID.fromString(orderId),
            IdempotencyStatus.PENDING
        );
    }


    public record OrderItem(
        @NotNull(message = "orderItemId는 필수입니다.")
        String orderItemId,

        @NotNull(message = "hubId는 필수입니다.")
        String hubId
    ) {

    }


}

