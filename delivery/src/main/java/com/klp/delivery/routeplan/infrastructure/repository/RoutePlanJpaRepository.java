package com.klp.delivery.routeplan.infrastructure.repository;

import com.klp.delivery.routeplan.domain.model.RoutePlan;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutePlanJpaRepository extends JpaRepository<RoutePlan, UUID> {
    @Modifying
    @Query("update RoutePlan r set r.deletedAt = CURRENT_TIMESTAMP " +
        "where r.departureId = :dep and r.arrivalId = :arr")
    void softDeleteByDepartureAndArrival(@Param("dep") UUID dep, @Param("arr") UUID arr);

    Optional<RoutePlan> findDeletedAtIsNotNullByDepartureIdAndArrivalId(UUID departureId, UUID arrivalId);

    boolean existsByDepartureIdAndArrivalId(UUID dep, UUID arr);
}
