package com.klp.delivery.delivery.application.command;

import java.util.UUID;

public record DeliveryCommand(

    UUID vendorDriverId,
    UUID orderId,
    UUID departureId,
    UUID arrivalId,
    UUID receiverId,
    String receiverName,
    String address,
    String receiverSlackId

) {

}
