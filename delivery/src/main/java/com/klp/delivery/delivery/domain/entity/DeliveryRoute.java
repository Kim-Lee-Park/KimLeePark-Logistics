package com.klp.delivery.delivery.domain.entity;

import com.klp.delivery.common.entity.BaseEntity;
import com.klp.delivery.common.enums.DeliveryRouteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Getter
@Table(name = "p_delivery_routes", schema = "delivery_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRoute extends BaseEntity {

    @Id
    @Column(name = "delivery_route_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryRouteId;

    @Comment("배송 ID")
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Comment("배송담당자 ID")
    @Column(name = "drvier_id", nullable = false)
    private Long driverId;

    @Comment("출발 허브 ID")
    @Column(name = "departure_id", nullable = false)
    private UUID departureId;

    @Comment("출발 허브 이름")
    @Column(name = "departure_name", nullable = false)
    private String departureName;

    @Comment("도착 허브 ID")
    @Column(name = "arrival_id", nullable = false)
    private UUID arrivalId;

    @Comment("도착 허브 이름")
    @Column(name = "arrival_name", nullable = false)
    private String arrivalName;

    @Comment("시퀀스")
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Comment("예상 거리")
    @Column(name = "estimated_distance", nullable = false)
    private Double estimatedDistance;

    @Comment("예상 소요시간")
    @Column(name = "estimated_time", nullable = false)
    private Long estimatedTime;

    @Comment("실제 거리")
    @Column(name = "real_distance", nullable = false)
    private Double realDistance;

    @Comment("실제 시간")
    @Column(name = "real_time", nullable = false)
    private Long realTime;

    @Comment("배송상태")
    @Enumerated(EnumType.STRING)
    private DeliveryRouteStatus status;


    private DeliveryRoute(UUID deliveryId, Long driverId, UUID departureId, String departureName,
        UUID arrivalId, String arrivalName, Integer sequence, Double estimatedDistance, Long estimatedTime,
        Double realDistance, Long realTime, DeliveryRouteStatus status) {
        this.deliveryId = deliveryId;
        this.driverId = driverId;
        this.departureId = departureId;
        this.departureName = departureName;
        this.arrivalId = arrivalId;
        this.arrivalName = arrivalName;
        this.sequence = sequence;
        this.estimatedDistance = estimatedDistance;
        this.estimatedTime = estimatedTime;
        this.realDistance = realDistance;
        this.realTime = realTime;
        this.status = status;
    }

    public static DeliveryRoute create(UUID deliveryId, Long driverId, UUID departureId, String departureName,
        UUID arrivalId, String arrivalName, Integer sequence, Double estimatedDistance, Long estimatedTime,
        Double realDistance, Long realTime, DeliveryRouteStatus status) {
        return new DeliveryRoute(deliveryId, driverId, departureId, departureName, arrivalId, arrivalName,  sequence,
            estimatedDistance, estimatedTime, realDistance, realTime, status);
    }

}
