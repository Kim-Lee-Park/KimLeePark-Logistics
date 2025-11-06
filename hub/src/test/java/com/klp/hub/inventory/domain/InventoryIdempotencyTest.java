package com.klp.hub.inventory.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryIdempotencyTest {

    @Test
    @DisplayName("멱등키가 존재하지 않는다면 예외가 발생한다")
    void nullIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryIdempotency(null));
    }
}
