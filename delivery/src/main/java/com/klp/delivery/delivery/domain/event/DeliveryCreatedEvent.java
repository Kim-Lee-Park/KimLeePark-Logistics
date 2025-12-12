package com.klp.delivery.delivery.domain.event;

import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import java.util.List;
import java.util.UUID;

public record DeliveryCreatedEvent(
    UUID orderId,
    List<OrderItem> items

) {

    public static DeliveryCreatedEvent from(OrderToDeliveryCommand orderCommand,
        List<OrderItem> allEventItems) {
        return new DeliveryCreatedEvent(orderCommand.orderId(), allEventItems);
    }

    public record OrderItem(
        UUID orderItemId,
        UUID deliveryId
    ) {

    }
}