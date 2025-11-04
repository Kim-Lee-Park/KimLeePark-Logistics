package com.klp.hub.inventory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "p_inventory")
@Getter
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Comment("재고 수량")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public Inventory() {
        this.quantity = 0;
    }

    public Inventory(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException();
        }
        this.quantity = quantity;
    }

    public void increase(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException();
        }

        this.quantity += quantity;
    }

    public void decrease(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException();
        }

        this.quantity -= quantity;
    }
}
