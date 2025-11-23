package com.klp.delivery.routeplan.presentation.dto.response;

import com.klp.delivery.routeplan.domain.model.RoutePlanItem;
import java.util.UUID;

public record GetRoutePlanItemDetailResponse(
    UUID planItemId,
    UUID planId,
    UUID departureId,
    UUID arrivalId,
    Long durationMin,
    Double distanceKm,
    Integer sequence
) {

    public static GetRoutePlanItemDetailResponse from(RoutePlanItem routePlanItem, UUID planId) {
        return new GetRoutePlanItemDetailResponse(
            routePlanItem.getRoutePlanItemId(),
            planId,
            routePlanItem.getDepartureId(),
            routePlanItem.getArrivalId(),
            routePlanItem.getDurationMin(),
            routePlanItem.getDistanceKm(),
            routePlanItem.getSequence()
        );
    }
}
