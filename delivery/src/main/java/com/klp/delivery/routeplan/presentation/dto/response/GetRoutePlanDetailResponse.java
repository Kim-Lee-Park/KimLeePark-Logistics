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
    List<PlanItem> planItems
) {
    public record PlanItem(
        UUID routePlanItemId,
        UUID departureId,
        UUID arrivalId,
        Long durationMin,
        Double distanceKm
    ){
        public static PlanItem from(RoutePlanItem routePlanItem) {
            return new PlanItem(
                routePlanItem.getRoutePlanItemId(),
                routePlanItem.getDepartureId(),
                routePlanItem.getArrivalId(),
                routePlanItem.getDurationMin(),
                routePlanItem.getDistanceKm()
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
            routePlan.getRoutePlanItems().stream().map(PlanItem::from).toList()
        );
    }
}
