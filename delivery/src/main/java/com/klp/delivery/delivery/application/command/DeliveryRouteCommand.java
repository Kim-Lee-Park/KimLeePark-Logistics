package com.klp.delivery.delivery.application.command;

import java.util.UUID;

public record DeliveryRouteCommand(
    UUID deliveryId,
    UUID departureId,
    UUID arrivalId,
    Long vendorDrvierId
) {

}
