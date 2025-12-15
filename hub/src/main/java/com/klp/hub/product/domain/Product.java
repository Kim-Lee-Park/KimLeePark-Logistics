package com.klp.hub.product.domain;

import com.klp.hub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "p_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false)
    private UUID id;

    @Comment("회사 ID")
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Comment("상품명")
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("카테고리")
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private ProductCategory category;

    public Product(UUID companyId, String name) {
        this(companyId, name, null);
    }

    public Product(UUID companyId, String name, ProductCategory category) {
        validateName(name);

        if (companyId == null) {
            throw new IllegalArgumentException("업체 ID는 필수입니다.");
        }
        this.name = name;
        this.companyId = companyId;
        this.category = category;
    }

    public void updateName(String name) {
        validateName(name);

        this.name = name;
    }

    public void updateCategory(ProductCategory category) {
        this.category = category;
    }

    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : "기타";
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
    }
}
