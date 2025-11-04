package com.klp.hub.company.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "p_companies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id", nullable = false)
    private UUID id;

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

    public Company(CompanyType type, String name, String address) {
        if (type == null) {
            throw new IllegalArgumentException("업체 종류는 필수입니다.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("업체명은 필수입니다.");
        }

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("업체명은 필수입니다.");
        }

        this.type = type;
        this.name = name;
        this.address = address;
    }
}
