package com.klp.delivery.routeplan.infrastructure.repository;

import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.domain.repository.RoutePlanRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoutePlanRepositoryImpl implements RoutePlanRepository {
    private final RoutePlanJpaRepository routePlanJpaRepository;

    @Override
    public RoutePlan save(RoutePlan routePlan) {
        return routePlanJpaRepository.save(routePlan);
    }

    @Override
    public void softDeleteByDepartureAndArrival(UUID departureId, UUID arrivalId) {
        routePlanJpaRepository.softDeleteByDepartureAndArrival(departureId, arrivalId);
    }

    @Override
    public Optional<RoutePlan> findByDepartureIdAndArrivalIdAndDeletedAtIsNull(UUID departureId,
        UUID arrivalId) {
        return routePlanJpaRepository.findByDepartureIdAndArrivalIdAndDeletedAtIsNull(departureId, arrivalId);
    }

    @Override
    public boolean existsByDepartureIdAndArrivalId(UUID departureId, UUID arrivalId) {
        return routePlanJpaRepository.existsByDepartureIdAndArrivalId(departureId, arrivalId);
    }

    @Override
    public Optional<RoutePlan> getRouteInfoById(UUID routePlanId) {
        return routePlanJpaRepository.findByRoutePlanIdAndDeletedAtIsNull(routePlanId);
    }

    @Override
    public Optional<RoutePlan> findByRoutePlanIdAndDeletedAtIsNull(UUID routePlanId) {
        return routePlanJpaRepository.findByRoutePlanIdAndDeletedAtIsNull(routePlanId);
    }
}
