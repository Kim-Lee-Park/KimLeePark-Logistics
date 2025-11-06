package com.klp.hub.inventory.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private UUID hubId = UUID.randomUUID();

    private UUID productId = UUID.randomUUID();

    @Test
    @DisplayName("재고 수량은 음수가 될 수 없다")
    void negativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> inventory(-1));
    }

    @Test
    @DisplayName("재고 수량은 null이 될 수 없다")
    void nullInventory() {
        assertThrows(IllegalArgumentException.class, () -> inventory(null));
    }

    @Test
    @DisplayName("재고 증가시 인자가 음수가 될 수 없다")
    void increaseNegativeQuantity() {
        Inventory inventory = inventory(0);
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.increase(-1);
        });
    }

    @Test
    @DisplayName("재고의 수량을 증가시킬 수 있다")
    void increaseQuantity() {
        Inventory inventory = inventory(1);

        inventory.increase(1);

        assertEquals(2, inventory.getQuantity());
    }

    @Test
    @DisplayName("재고 차감시 인자가 음수가 될 수 없다")
    void decreaseNegativeQuantity() {
        Inventory inventory = inventory(0);
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.decrease(-1);
        });
    }

    @Test
    @DisplayName("기존 재고 수량보다 더 많은 수량을 차감할 수 없다")
    void betterThanQuantity() {
        Inventory inventory = inventory(100);
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.decrease(101);
        });
    }

    @Test
    @DisplayName("재고의 수량을 차감시킬 수 있다")
    void decreaseQuantity() {
        Inventory inventory = inventory(1);

        inventory.decrease(1);

        assertEquals(0, inventory.getQuantity());
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 예외가 발생한다")
    void throwNullProduct() {
        assertThrows(IllegalArgumentException.class, () -> new Inventory(null, hubId));
    }

    @Test
    @DisplayName("허브가 존재하지 않으면 예외가 발생한다")
    void throwNullHub() {
        assertThrows(IllegalArgumentException.class, () -> new Inventory(productId, null));
    }

    private Inventory inventory(Integer quantity) {
        return new Inventory(UUID.randomUUID(), UUID.randomUUID(), quantity);
    }
}
