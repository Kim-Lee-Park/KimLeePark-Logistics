package com.klp.hub.hub.domain.model;

import com.klp.hub.common.entity.BaseEntity;
import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
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

@Entity
@Table(name = "p_hubs", schema = "hub_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID hubId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long latitude;

    @Column(nullable = false)
    private Long longitude;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private HubStatus status;

    public static Hub create(RegisterHubCommand command){
        Hub hub = new Hub();
        hub.name= command.name();
        hub.latitude = command.latitude();
        hub.longitude = command.longitude();
        hub.address = command.address();
        hub.status=HubStatus.ACTIVE;
        return hub;
    }
}
