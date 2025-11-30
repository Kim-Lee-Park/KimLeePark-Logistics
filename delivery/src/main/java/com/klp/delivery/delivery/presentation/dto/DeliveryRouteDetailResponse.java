package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.enums.DeliveryStatus;
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
    DeliveryStatus status
) {

}
