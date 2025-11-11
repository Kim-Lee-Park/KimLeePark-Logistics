package com.klp.delivery.delivery.application.command;

import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import java.util.List;
import java.util.UUID;

public record OrderToDeliveryCommand(
    UUID orderId,
    UUID senderId,
    UUID receiverId,
    List<OrderItemCommand> items
) {

    public record OrderItemCommand(
        UUID orderItemId,
        UUID hubId
    ) {


    }
}