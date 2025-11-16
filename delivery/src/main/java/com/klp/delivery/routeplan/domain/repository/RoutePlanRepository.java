package com.klp.delivery.routeplan.domain.repository;

import com.klp.delivery.routeplan.domain.model.RoutePlan;
import java.util.Optional;
import java.util.UUID;

public interface RoutePlanRepository {
    RoutePlan save(RoutePlan routePlan);

    void softDeleteByDepartureAndArrival(UUID departureId, UUID arrivalId);

    Optional<RoutePlan> findByDepartureIdAndArrivalIdAndDeletedAtIsNull(UUID departureId, UUID arrivalId);

    boolean existsByDepartureIdAndArrivalId(UUID departureId, UUID arrivalId);

    Optional<RoutePlan> getRouteInfoById(UUID routePlanId);

    Optional<RoutePlan> findByRoutePlanIdAndDeletedAtIsNull(UUID routePlanId);
}
