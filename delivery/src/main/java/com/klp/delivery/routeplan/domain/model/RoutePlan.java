package com.klp.delivery.routeplan.domain.model;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.entity.BaseEntity;
import com.klp.delivery.routeplan.exception.RoutePlanErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_route_plans", schema = "delivery_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePlan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID routePlanId;

    @Column(nullable = false)
    private UUID departureId;
    @Column(nullable = false)
    private UUID arrivalId;

    @Column(nullable = false)
    private Long totalDurationMin;
    @Column(nullable = false)
    private Double totalDistanceKm;

    @OneToMany(mappedBy = "routePlan",fetch = FetchType.LAZY, cascade = CascadeType.ALL,orphanRemoval = true)
    private List<RoutePlanItem> routePlanItems=new ArrayList<>();

    public static RoutePlan create(UUID departureId, UUID arrivalId, long totalDurationMin, double totalDistanceKm) {
        validate(departureId,arrivalId,totalDurationMin,totalDistanceKm);
        RoutePlan routePlan=new RoutePlan();
        routePlan.departureId = departureId;
        routePlan.arrivalId = arrivalId;
        routePlan.totalDurationMin = totalDurationMin;
        routePlan.totalDistanceKm = totalDistanceKm;
        return routePlan;
    }

    private static void validate(UUID departureId, UUID arrivalId, long totalDurationMin, double totalDistanceKm) {
        if (departureId == null) throw new BusinessException(RoutePlanErrorCode.DEPARTURE_ID_REQUIRED);
        if (arrivalId == null) throw new BusinessException(RoutePlanErrorCode.ARRIVAL_ID_REQUIRED);
        if (totalDurationMin <= 0) throw new BusinessException(RoutePlanErrorCode.DURATION_INVALID);;
        if (totalDistanceKm <= 0) throw new BusinessException(RoutePlanErrorCode.DISTANCE_INVALID);
    }
}
