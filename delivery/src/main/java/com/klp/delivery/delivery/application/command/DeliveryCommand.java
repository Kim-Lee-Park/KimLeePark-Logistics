package com.klp.delivery.delivery.application.command;

import java.util.UUID;

public record DeliveryCommand(
    UUID orderId,
    UUID orderItemId,
    UUID departureId,
    UUID arrivalId,
    UUID senderId,
    UUID receiverId,
    String receiverName,
    String address,
    String receiverSlackId,
    Long vendorDriverId
) {

}

