package com.klp.delivery.delivery.domain.event;

import java.util.UUID;

public record DeliveryRouteCreateEvent(
    UUID deliveryId,
    UUID departureId,
    UUID arrivalId,
    Long vendorDrvierId
) {

}
