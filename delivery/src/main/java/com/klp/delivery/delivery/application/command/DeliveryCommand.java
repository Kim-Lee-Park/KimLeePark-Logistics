package com.klp.delivery.delivery.application.command;

import java.util.UUID;

public record DeliveryCommand(
    UUID orderId,
    UUID departureId,
    String departureName,
    UUID arrivalId,
    String arrivalName,
    String userName,
    String userAddress,
    String userDrvicerSlackId,
    Long userDriverId
) {

}

