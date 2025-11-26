package com.klp.delivery.routeplan.domain.repository;

import com.klp.delivery.common.enums.RoutePlanStatus;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.domain.model.RoutePlanItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoutePlanRepository {

    RoutePlan save(RoutePlan routePlan);

    void softDeleteByDepartureAndArrival(UUID departureId, UUID arrivalId);

    Optional<RoutePlan> findByDepartureIdAndArrivalIdAndDeletedAtIsNull(UUID departureId,
        UUID arrivalId);

    boolean existsByDepartureIdAndArrivalId(UUID departureId, UUID arrivalId);

    Optional<RoutePlan> getRoutePlanById(UUID routePlanId);

    Optional<RoutePlan> findByRoutePlanIdAndDeletedAtIsNull(UUID routePlanId);

    Page<RoutePlan> findAll(UUID depId, UUID arrId, Pageable pageable);

    Optional<RoutePlanItem> findRoutePlanItemById(UUID routePlanId);

    List<RoutePlan> findByHubId(UUID hubId);

    List<RoutePlan> findAllByStatus(RoutePlanStatus routePlanStatus);
}
