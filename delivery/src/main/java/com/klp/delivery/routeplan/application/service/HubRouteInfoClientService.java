package com.klp.delivery.routeplan.application.service;

import com.klp.delivery.routeplan.application.command.HubRouteInfo;
import java.util.List;
import java.util.UUID;

public interface HubRouteInfoClientService {
    HubRouteInfo getHubRouteInfo(UUID departureId,UUID arrivalId);

    List<HubRouteInfo> getHubRouteInfos();
}
