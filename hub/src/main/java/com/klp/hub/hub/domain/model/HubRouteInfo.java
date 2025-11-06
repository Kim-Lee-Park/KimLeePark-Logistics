package com.klp.hub.hub.domain.model;

import com.klp.hub.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "p_hub_route_infos", schema = "hub_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubRouteInfo extends BaseEntity {

    @Id
    @UuidGenerator
    private UUID hubRouteId;

    private UUID departureId;
    private UUID arrivalId;

    private Long durationMin;
    private Double distanceKm;
}