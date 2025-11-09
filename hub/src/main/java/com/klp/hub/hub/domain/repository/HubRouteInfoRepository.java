package com.klp.hub.hub.domain.repository;

import com.klp.hub.hub.domain.model.HubRouteInfo;
import com.querydsl.core.BooleanBuilder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRouteInfoRepository {
    HubRouteInfo save(HubRouteInfo hubRouteInfo);

    Optional<HubRouteInfo> getHubRouteInfoById(UUID hubRouteInfoId);

    Page<HubRouteInfo> getHubRoutes(UUID depId, UUID arrId, Pageable pageable);
}
