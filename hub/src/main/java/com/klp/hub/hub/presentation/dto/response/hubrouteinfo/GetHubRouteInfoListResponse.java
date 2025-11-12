package com.klp.hub.hub.presentation.dto.response.hubrouteinfo;

import com.klp.hub.common.dto.PageableDto;
import com.klp.hub.hub.domain.model.HubRouteInfo;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record GetHubRouteInfoListResponse(
    List<HubRouteInfoSummaryResponse> routeInfos,
    PageableDto pageable
) {

    public static GetHubRouteInfoListResponse from(Page<HubRouteInfo> page) {
        List<HubRouteInfoSummaryResponse> routeInfos = page.getContent().stream()
            .map(info -> new HubRouteInfoSummaryResponse(
                info.getHubRouteId(),
                info.getDepartureId(),
                info.getArrivalId(),
                info.getDurationMin(),
                info.getDistanceKm()
            ))
            .toList();

        PageableDto pageableDto = new PageableDto(
            page.getNumber(),
            page.getSize(),
            (int) page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious(),
            page.isFirst(),
            page.isLast()
        );

        return new GetHubRouteInfoListResponse(routeInfos,pageableDto);
    }

    public record HubRouteInfoSummaryResponse(
        UUID routeInfoId,
        UUID departureId,
        UUID arrivalId,
        Long durationMin,
        Double distanceKm
    ){}
}
