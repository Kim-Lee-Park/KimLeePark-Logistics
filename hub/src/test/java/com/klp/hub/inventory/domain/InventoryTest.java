package com.klp.hub.inventory.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    @DisplayName("재고 수량은 음수가 될 수 없다")
    void negativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Inventory(-1));
    }

    @Test
    @DisplayName("재고 수량은 null이 될 수 없다")
    void nullInventory() {
        assertThrows(IllegalArgumentException.class, () -> new Inventory(null));
    }

    @Test
    @DisplayName("재고 증가시 인자가 음수가 될 수 없다")
    void increaseNegativeQuantity() {
        Inventory inventory = new Inventory();
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.increase(-1);
        });
    }

    @Test
    @DisplayName("재고의 수량을 증가시킬 수 있다")
    void increaseQuantity() {
        Inventory inventory = new Inventory(1);

        inventory.increase(1);

        assertEquals(2, inventory.getQuantity());
    }

    @Test
    @DisplayName("재고 차감시 인자가 음수가 될 수 없다")
    void decreaseNegativeQuantity() {
        Inventory inventory = new Inventory();
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.decrease(-1);
        });
    }

    @Test
    @DisplayName("재고의 수량을 차감시킬 수 있다")
    void decreaseQuantity() {
        Inventory inventory = new Inventory(1);

        inventory.decrease(1);

        assertEquals(0, inventory.getQuantity());
    }
}
