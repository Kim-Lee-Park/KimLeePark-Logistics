package com.klp.hub.inventory.domain;

import com.klp.hub.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(
    name = "p_inventory",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_inventory_product_hub",
            columnNames = {"product_id", "hub_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_id", nullable = false)
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
        validHub(hubId);
        this.productId = productId;
        this.hubId = hubId;
        this.quantity = 0;
    }

    public Inventory(UUID productId, UUID hubId, Integer quantity) {
        validQuantity(quantity);
        validProduct(productId);
        validHub(hubId);
        this.productId = productId;
        this.hubId = hubId;
        this.quantity = quantity;
    }

    /**
     * @param quantity 재고 수량 재고 수량을 증가시킨다
     */
    public void replenish(Integer quantity) {
        validQuantity(quantity);

        this.quantity += quantity;
    }

    /**
     * @param quantity 재고 수량 재고 수량을 차감시킨다
     */
    public void deduct(Integer quantity) {
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

    private void validHub(UUID hubId) {
        if (hubId == null) {
            throw new IllegalArgumentException("허브는 필수입니다.");
        }
    }
}
