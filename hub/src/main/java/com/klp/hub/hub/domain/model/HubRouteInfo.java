package com.klp.hub.hub.domain.model;

import com.klp.hub.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "p_hub_route_infos", schema = "hub_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubRouteInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID hubRouteId;

    @Comment("출발 허브 ID")
    @Column(nullable = false)
    private UUID departureId;

    @Comment("도착 허브 ID")
    @Column(nullable = false)
    private UUID arrivalId;

    @Comment("소요 시간")
    @Column(nullable = false)
    private Long durationMin;

    @Comment("거리")
    @Column(nullable = false)
    private Double distanceKm;

    public static HubRouteInfo create(UUID departureId, UUID arrivalId, Long durationMin, Double distanceKm) {
        HubRouteInfo info = new HubRouteInfo();
        info.departureId = departureId;
        info.arrivalId = arrivalId;
        info.durationMin = durationMin;
        info.distanceKm = distanceKm;
        return info;
    }
}