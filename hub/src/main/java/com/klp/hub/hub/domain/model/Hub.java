package com.klp.hub.hub.domain.model;

import com.klp.hub.common.entity.BaseEntity;
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

    private String name;

    private Long latitude;

    private Long longitude;

    private String address;
}
