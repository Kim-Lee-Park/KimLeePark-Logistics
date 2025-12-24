package com.klp.hub.company.domain;

import com.klp.hub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "p_companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id", nullable = false)
    private UUID id;

    @Comment("허브 ID")
    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Enumerated(EnumType.STRING)
    @Comment("업체 종류")
    @Column(name = "type", nullable = false)
    private CompanyType type;

    @Comment("업체명")
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("업체 주소")
    @Column(name = "address", nullable = false)
    private String address;

    public Company(UUID hubId, CompanyType type, String name, String address) {
        if (hubId == null) {
            throw new IllegalArgumentException("허브 ID는 필수입니다.");
        }

        if (type == null) {
            throw new IllegalArgumentException("업체 종류는 필수입니다.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("업체명은 필수입니다.");
        }

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("업체명은 필수입니다.");
        }

        this.hubId = hubId;
        this.type = type;
        this.name = name;
        this.address = address;
    }
}
