package com.klp.delivery.delivery.application.command;

import java.util.UUID;

public record DeliveryRouteCommand(
    UUID deliveryId,
    UUID departureId,
    String departureName,
    UUID arrivalId,
    String arrivalName,
    Long driverId
) {

}
