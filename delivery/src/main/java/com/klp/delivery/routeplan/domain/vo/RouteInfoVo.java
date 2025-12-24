package com.klp.delivery.routeplan.domain.vo;

import java.util.UUID;

public record RouteInfoVo(
    UUID routeInfoId,
    UUID departureId,
    String departureName,
    UUID arrivalId,
    String arrivalName,
    Long durationMin,
    Double distanceKm
) {

}
