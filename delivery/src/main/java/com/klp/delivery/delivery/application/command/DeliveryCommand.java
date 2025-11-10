package com.klp.delivery.delivery.application.command;

import java.util.UUID;

public record DeliveryCommand(
    UUID orderId,
    UUID orderItemId,
    UUID hubId,
    UUID departureId,
    UUID arrivalId,
    UUID receiverId,
    String receiverName,
    String address,
    String receiverSlackId,
    UUID vendorDriverId
) {

}
