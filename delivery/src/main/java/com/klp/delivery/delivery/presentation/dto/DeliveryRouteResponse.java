package com.klp.delivery.delivery.presentation.dto;

import java.util.UUID;

public record DeliveryRouteResponse(
    UUID deliveryId,
    UUID routeId
) {

}

