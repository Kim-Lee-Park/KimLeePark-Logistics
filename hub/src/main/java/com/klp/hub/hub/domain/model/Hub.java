package com.klp.hub.hub.domain.model;

import com.klp.hub.common.entity.BaseEntity;
import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import com.klp.hub.hub.application.command.hub.UpdateHubCommand;
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

    public static Hub create(String name, Long latitude, Long longitude, String address) {
        Hub hub = new Hub();
        hub.name= name;
        hub.latitude = latitude;
        hub.longitude = longitude;
        hub.address = address;
        hub.status=HubStatus.ACTIVE;
        return hub;
    }

    public void update(String name, Long latitude, Long longitude, String address) {
        if(name != null){
            this.name = name;
        }
        if(latitude != null){
            this.latitude = latitude;
        }
        if(longitude != null){
            this.longitude = longitude;
        }
        if(address != null){
            this.address = address;
        }
    }
}
