package com.klp.delivery.routeplan.infrastructure.repository;

import com.klp.delivery.routeplan.domain.model.RoutePlanItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutePlanItemJpaRepository extends JpaRepository<RoutePlanItem, UUID> {

    Optional<RoutePlanItem> findByRoutePlanItemIdAndDeletedAtIsNull(UUID routePlanItemId);
}
