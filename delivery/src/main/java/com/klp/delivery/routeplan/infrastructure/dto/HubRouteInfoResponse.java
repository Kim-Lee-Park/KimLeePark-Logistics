package com.klp.delivery.routeplan.infrastructure.dto;

import com.klp.delivery.common.dto.PageableDto;
import com.klp.delivery.routeplan.application.command.HubRouteInfo;
import java.util.List;
import java.util.UUID;

public record HubRouteInfoResponse(
    List<RouteInfoItem> routeInfos,
    PageableDto pageable
) {
    public record RouteInfoItem(
        UUID routeInfoId,
        UUID departureId,
        String departureName,
        UUID arrivalId,
        String arrivalName,
        Long durationMin,
        Double distanceKm
    ){
        public HubRouteInfo toCommand(){
            return new HubRouteInfo(
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
}
