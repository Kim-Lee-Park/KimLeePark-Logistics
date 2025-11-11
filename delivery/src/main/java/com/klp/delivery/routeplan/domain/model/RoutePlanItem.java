package com.klp.delivery.routeplan.domain.model;

import com.klp.delivery.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_route_plan_items", schema = "delivery_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePlanItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID routePlanItemId;

    @Column(nullable = false)
    private UUID departureId;
    @Column(nullable = false)
    private UUID arrivalId;

    @Column(nullable = false)
    private Long durationMin;
    @Column(nullable = false)
    private Double distanceKm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_plan_id")
    private RoutePlan routePlan;

    @Column(nullable = false)
    private Integer sequence;
}
