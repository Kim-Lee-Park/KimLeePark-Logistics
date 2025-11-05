package com.klp.hub.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(
        name = "p_products",
        schema = "hub_schema"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false)
    private UUID id;

    @Comment("회사 ID")
    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Comment("상품명")
    @Column(name = "name", nullable = false)
    private String name;

    public Product(String companyId, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }

        if (companyId == null) {
            throw new IllegalArgumentException("업체 ID는 필수입니다.");
        }
        this.name = name;
        this.companyId = companyId;
    }
}
