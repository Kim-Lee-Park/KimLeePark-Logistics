package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.enums.DeliveryRouteStatus;
import java.util.UUID;

public record DeliveryRouteDetailResponse(
    UUID deliveryRouteId,
    UUID deliveryId,
    Long driverId,
    UUID departureId,
    UUID arrivalId,
    Integer sequence,
    Double estimatedDistance,
    Long estimatedTime,
    Double realDistance,
    Long realTime,
    DeliveryRouteStatus status
) {

}
