package com.klp.delivery.delivery.application.command;

import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import java.util.List;
import java.util.UUID;

public record DeliveryRoutePlanCommand(
    UUID routePlanId,
    UUID departureId,
    UUID arrivalId,
    Long totalDurationMin,
    Double totalDistanceKm,
    List<PlanItem> planItems
) {

    public static DeliveryRoutePlanCommand toDeliveryRoutePlanCommand(
        GetRoutePlanDetailResponse response) {
        return new DeliveryRoutePlanCommand(
            response.routePlanId(),
            response.departureId(),
            response.arrivalId(),
            response.totalDurationMin(),
            response.totalDistanceKm(),
            response.planItems().stream()
                .map(item -> new PlanItem(
                    item.routePlanItemId(),
                    item.planId(),
                    item.departureId(),
                    item.arrivalId(),
                    item.durationMin(),
                    item.distanceKm(),
                    item.sequence()
                ))
                .toList()
        );

    }


    public record PlanItem(
        UUID routePlanItemId,
        UUID planId,
        UUID departureId,
        UUID arrivalId,
        Long durationMin,
        Double distanceKm,
        Integer sequence
    ) {


    }

}
