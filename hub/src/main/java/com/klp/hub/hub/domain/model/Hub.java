package com.klp.hub.hub.domain.model;

import com.klp.hub.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_hubs",
    schema = "hub_schema",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_hub_name",
            columnNames = {"name"}
        ),
        @UniqueConstraint(
            name = "uk_hub_address",
            columnNames = {"address"}
        )
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID hubId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HubStatus status;

    public static Hub create(String name, Double latitude, Double longitude, String address) {
        Hub hub = new Hub();
        hub.name = name;
        hub.latitude = latitude;
        hub.longitude = longitude;
        hub.address = address;
        hub.status = HubStatus.ACTIVE;
        return hub;
    }

    public void update(String name, Double latitude, Double longitude, String address) {
        if (name != null) {
            this.name = name;
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
        if (address != null) {
            this.address = address;
        }
    }

    public void softDelete() {
        this.setDeletedAt(LocalDateTime.now());
        this.status = HubStatus.DELETED;
    }

    public void pendingDelete(Long userId) {
        this.status = HubStatus.PENDING_DELETE;
        setDeletedBy(userId);
    }
}
