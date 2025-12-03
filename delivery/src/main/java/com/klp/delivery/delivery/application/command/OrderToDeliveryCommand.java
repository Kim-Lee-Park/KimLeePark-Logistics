package com.klp.delivery.delivery.application.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderToDeliveryCommand(
    UUID orderId,
    String name,
    String email,
    String address,
    UUID userAddressHubId,
    LocalDateTime orderCreateAt,
    String comment,
    List<OrderItemCommand> items
) {

    public record OrderItemCommand(
        UUID orderItemId,
        UUID hubId,
        String productName,
        int quantity
    ) {


    }
}