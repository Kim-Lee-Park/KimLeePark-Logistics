package com.klp.delivery.routeplan.application.command;

import com.klp.delivery.routeplan.domain.vo.RouteInfoVo;
import java.util.UUID;

public record HubRouteInfo(
    UUID routeInfoId,
    UUID departureId,
    String departureName,
    UUID arrivalId,
    String arrivalName,
    Long durationMin,
    Double distanceKm
) {
    public RouteInfoVo toVo() {
        return new RouteInfoVo(
            routeInfoId,
            departureId,
            departureName,
            arrivalId,
            arrivalName,
            durationMin,
            distanceKm
        );
    }
}
