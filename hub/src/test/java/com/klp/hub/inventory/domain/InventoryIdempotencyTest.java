package com.klp.hub.inventory.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryIdempotencyTest {

    @Test
    @DisplayName("멱등키가 존재하지 않는다면 예외가 발생한다")
    void nullIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryIdempotency(null));
    }

    @Test
    @DisplayName("멱등키가 빈 값이라면 예외가 발생한다")
    void emptyIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryIdempotency(" "));
    }
}
