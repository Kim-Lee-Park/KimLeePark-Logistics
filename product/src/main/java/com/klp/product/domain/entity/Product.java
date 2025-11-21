package com.klp.product.domain.entity;

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
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "p_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Comment("상품명")
    @Column(name = "name", nullable = false)
    private String name;

    private Integer stock;

    public Product(String name) {
        validateName(name);
        this.name = name;
        this.stock = 0;
    }

    public Product(String name, Integer stock) {
        validateName(name);
        this.name = name;
        this.stock = stock;
    }

    public void decreaseStock(Integer stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("차감할 수량은 음수값이 될 수 없습니다.");
        }
        if (this.stock < stock) {
            throw new IllegalArgumentException("재고가 부족합니다");
        }
        this.stock = this.stock - stock;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수값입니다.");
        }
    }
}
