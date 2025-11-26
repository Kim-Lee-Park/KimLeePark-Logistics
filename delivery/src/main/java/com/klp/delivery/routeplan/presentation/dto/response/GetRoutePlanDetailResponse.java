package com.klp.delivery.routeplan.presentation.dto.response;

import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.domain.model.RoutePlanItem;
import java.util.List;
import java.util.UUID;

public record GetRoutePlanDetailResponse(
    UUID routePlanId,
    UUID departureId,
    UUID arrivalId,
    Long totalDurationMin,
    Double totalDistanceKm,
    List<PlanItem> planItems,
    String status
) {

    public record PlanItem(
        UUID routePlanItemId,
        UUID planId,
        UUID departureId,
        UUID arrivalId,
        Long durationMin,
        Double distanceKm,
        Integer sequence
    ) {

        public static PlanItem from(RoutePlanItem routePlanItem, UUID planId) {
            return new PlanItem(
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

    public static GetRoutePlanDetailResponse from(RoutePlan routePlan) {
        return new GetRoutePlanDetailResponse(
            routePlan.getRoutePlanId(),
            routePlan.getDepartureId(),
            routePlan.getArrivalId(),
            routePlan.getTotalDurationMin(),
            routePlan.getTotalDistanceKm(),
            routePlan.getRoutePlanItems().stream()
                .map(item -> PlanItem.from(item, routePlan.getRoutePlanId())).toList(),
            routePlan.getStatus().name()
        );
    }
}
