package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.enums.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryCreateRequest(
    @NotNull(message = "orderId는 필수입니다.")
    String orderId,

    @NotBlank(message = "idempotencyKey는 필수입니다.")
    String idempotencykey,

    @NotBlank(message = "고객명은 필수입니다.")
    String name,

    @NotBlank(message = "고객 이메일 주소 필수입니다.")
    String email,

    @NotBlank(message = "고객 주소는 필수입니다.")
    String address,

    @NotBlank(message = "고객 주소지 허브 ID는 필수입니다.")
    String userAddressHubId,

    @NotBlank(message = "주문시간은 필수입니다.")
    LocalDateTime orderCreateAt,

    String comment,

    @NotEmpty(message = "orderItems는 필수이며 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItem> orderItems
) {

    public OrderToDeliveryCommand toOrderToDeliveryCommand() {
        return new OrderToDeliveryCommand(
            UUID.fromString(orderId),
            name,
            email,
            address,
            UUID.fromString(userAddressHubId),
            orderCreateAt,
            comment,
            orderItems.stream()
                .map(item -> new OrderItemCommand(
                    UUID.fromString(item.orderItemId()),
                    UUID.fromString(item.hubId()),
                    item.productName(),
                    item.quantity()
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
        @NotBlank(message = "orderItemId는 필수입니다.")
        String orderItemId,

        @NotBlank(message = "hubId는 필수입니다.")
        String hubId,

        @NotBlank(message = "상품명은 필수 입니다")
        String productName,

        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        int quantity


    ) {

    }


}

