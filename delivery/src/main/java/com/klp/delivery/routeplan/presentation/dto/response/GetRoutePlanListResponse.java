package com.klp.delivery.routeplan.presentation.dto.response;

import com.klp.delivery.common.dto.PageableDto;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record GetRoutePlanListResponse(
    List<RoutePlanSummary> routePlans,
    PageableDto pageable
) {
    public record RoutePlanSummary(
        UUID routePlanId,
        UUID departureId,
        UUID arrivalId,
        Long totalDurationMin,
        Double totalDistanceKm
    ){
        public static RoutePlanSummary from(RoutePlan routePlan){
            return new RoutePlanSummary(
                routePlan.getRoutePlanId(),
                routePlan.getDepartureId(),
                routePlan.getArrivalId(),
                routePlan.getTotalDurationMin(),
                routePlan.getTotalDistanceKm()
            );
        }
    }

    public static GetRoutePlanListResponse from(Page<RoutePlan> routePlans){
        List<RoutePlanSummary> list = routePlans.getContent().stream().map(RoutePlanSummary::from).toList();
        PageableDto pageableDto = new PageableDto(
            routePlans.getNumber(),
            routePlans.getSize(),
            (int) routePlans.getTotalElements(),
            routePlans.getTotalPages(),
            routePlans.hasNext(),
            routePlans.hasPrevious(),
            routePlans.isFirst(),
            routePlans.isLast()
        );
        return new  GetRoutePlanListResponse(list, pageableDto);
    }
}
