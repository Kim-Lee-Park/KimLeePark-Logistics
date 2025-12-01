package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.enums.DeliveryRouteStatus;
import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
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

    public static DeliveryRouteDetailResponse from(DeliveryRoute route) {
        return new DeliveryRouteDetailResponse(
            route.getDeliveryRouteId(),
            route.getDeliveryId(),
            route.getDriverId(),
            route.getDepartureHubId(),
            route.getArrivalHubId(),
            route.getSequence(),
            route.getEstimatedDistance(),
            route.getEstimatedTime(),
            route.getRealDistance(),
            route.getRealTime(),
            route.getStatus()
        );
    }

}
