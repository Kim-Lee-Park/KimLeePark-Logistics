package com.klp.delivery.routeplan.domain.vo;

import java.util.UUID;

public record RouteInfoVo(
    UUID routeInfoId,
    UUID departureId,
    UUID arrivalId,
    Long durationMin,
    Double distanceKm
) {

}
