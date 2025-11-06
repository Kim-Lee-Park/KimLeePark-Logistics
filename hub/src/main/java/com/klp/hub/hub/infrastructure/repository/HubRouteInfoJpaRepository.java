package com.klp.hub.hub.infrastructure.repository;

import com.klp.hub.hub.domain.model.HubRouteInfo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRouteInfoJpaRepository extends JpaRepository<HubRouteInfo, UUID> {

}
