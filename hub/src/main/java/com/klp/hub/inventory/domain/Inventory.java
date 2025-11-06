package com.klp.hub.inventory.domain;

import com.klp.hub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(
        name = "p_inventory",
        schema = "hub_schema"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invevntory_id", nullable = false)
    private UUID id;

    @Comment("재고 수량")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Comment("상품 ID")
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Comment("허브 ID")
    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    public Inventory(UUID productId, UUID hubId) {
        validProduct(productId);
        this.productId = productId;
        this.hubId = hubId;
        this.quantity = 0;
    }

    public Inventory(UUID productId, UUID hubId, Integer quantity) {
        validQuantity(quantity);
        validProduct(productId);
        this.productId = productId;
        this.hubId = hubId;
        this.quantity = quantity;
    }

    public void increase(Integer quantity) {
        validQuantity(quantity);

        this.quantity += quantity;
    }

    public void decrease(Integer quantity) {
        validQuantity(quantity);

        if (this.quantity - quantity < 0) {
            throw new IllegalArgumentException("기존 재고 수량을 초과하여 차감하였습니다.");
        }

        this.quantity -= quantity;
    }

    private void validQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 필수이면서 음수일 수 없습니다.");
        }
    }

    private void validProduct(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("상품은 필수입니다.");
        }
    }
}
