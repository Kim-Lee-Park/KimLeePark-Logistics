package com.klp.delivery.routeplan.domain.model;

import com.klp.delivery.common.entity.BaseEntity;
import com.klp.delivery.routeplan.domain.vo.RouteInfoVo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_route_plan_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePlanItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID routePlanItemId;

    @Column(nullable = false)
    private UUID departureId;

    @Column(nullable = false)
    private String departureName;

    @Column(nullable = false)
    private UUID arrivalId;

    @Column(nullable = false)
    private String arrivalName;

    @Column(nullable = false)
    private Long durationMin;
    @Column(nullable = false)
    private Double distanceKm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_plan_id")
    private RoutePlan routePlan;

    @Column(nullable = false)
    private Integer sequence;

    public static RoutePlanItem create(UUID departureId, String departureName, UUID arrivalId, String arrivalName, long totalDurationMin,
        double totalDistanceKm, int sequence,RoutePlan routePlan) {
        RoutePlanItem routePlanItem = new RoutePlanItem();
        routePlanItem.departureId = departureId;
        routePlanItem.departureName = departureName;
        routePlanItem.arrivalId = arrivalId;
        routePlanItem.arrivalName = arrivalName;
        routePlanItem.durationMin = totalDurationMin;
        routePlanItem.distanceKm = totalDistanceKm;
        routePlanItem.sequence = sequence;
        routePlanItem.routePlan = routePlan;
        return routePlanItem;
    }

    public static RoutePlanItem from(RouteInfoVo info, int i, RoutePlan routePlan, String departureName, String arrivalName) {
        RoutePlanItem routePlanItem = new RoutePlanItem();
        routePlanItem.departureId = info.departureId();
        routePlanItem.departureName = departureName;
        routePlanItem.arrivalId = info.arrivalId();
        routePlanItem.arrivalName = arrivalName;
        routePlanItem.durationMin = info.durationMin();
        routePlanItem.distanceKm = info.distanceKm();
        routePlanItem.sequence = i;
        routePlanItem.routePlan = routePlan;
        return routePlanItem;
    }
}
