package com.klp.hub.hub.infrastructure.repository;

import com.klp.hub.hub.domain.model.HubRouteInfo;
import com.klp.hub.hub.domain.repository.HubRouteInfoRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubRouteInfoRepositoryImpl implements HubRouteInfoRepository {
    private final HubRouteInfoJpaRepository hubRouteInfoJpaRepository;

    @Override
    public HubRouteInfo save(HubRouteInfo hubRouteInfo) {
        return hubRouteInfoJpaRepository.save(hubRouteInfo);
    }

    @Override
    public Optional<HubRouteInfo> getHubRouteInfoById(UUID hubRouteInfoId) {
        return hubRouteInfoJpaRepository.findById(hubRouteInfoId);
    }

    @Override
    public Page<HubRouteInfo> getHubRoutes(Pageable pageable) {
        return hubRouteInfoJpaRepository.findAll(pageable);
    }
}
