package com.klp.hub.inventory.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
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

    @Test
    @DisplayName("재고 멱등 엔티티가 생성되면 상태는 IN_PROGRESS 이다")
    void pending() {
        InventoryIdempotency inventoryIdempotency = new InventoryIdempotency(
            UUID.randomUUID().toString()
        );

        assertEquals(InventoryIdempotencyStatus.IN_PROGRESS, inventoryIdempotency.getStatus());
    }

    @Test
    @DisplayName("요청이 성공하면 상태는 SUCCESS 이다")
    void success() {
        InventoryIdempotency inventoryIdempotency = new InventoryIdempotency(
            UUID.randomUUID().toString()
        );

        inventoryIdempotency.success();

        assertEquals(InventoryIdempotencyStatus.SUCCESS, inventoryIdempotency.getStatus());
    }

    @Test
    @DisplayName("이미 성공한 멱등키라면 isUsed 가 true 이다")
    void isUsedTrue() {
        InventoryIdempotency inventoryIdempotency = new InventoryIdempotency(
            UUID.randomUUID().toString()
        );

        inventoryIdempotency.success();

        assertTrue(inventoryIdempotency.isUsed());
    }

    @Test
    @DisplayName("처리중인 멱등키라면 isUsed 가 false 이다")
    void isUsedFalse() {
        InventoryIdempotency inventoryIdempotency = new InventoryIdempotency(
            UUID.randomUUID().toString()
        );

        assertFalse(inventoryIdempotency.isUsed());
    }
}
