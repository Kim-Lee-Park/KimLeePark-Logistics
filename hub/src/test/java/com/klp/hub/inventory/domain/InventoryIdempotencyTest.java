package com.klp.hub.inventory.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.klp.hub.inventory.exception.InventoryErrorCode;
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
    @DisplayName("재고 멱등 엔티티가 생성되면 상태는 PENDING 이다")
    void pending() {
        InventoryIdempotency inventoryIdempotency = new InventoryIdempotency(
            UUID.randomUUID().toString()
        );

        assertEquals(InventoryIdempotencyStatus.PENDING, inventoryIdempotency.getStatus());
    }

    @Test
    @DisplayName("요청이 비즈니스 로직에 의해 실패하면 상태는 FAILED 이다")
    void failed() {
        InventoryIdempotency inventoryIdempotency = new InventoryIdempotency(
            UUID.randomUUID().toString()
        );

        inventoryIdempotency.failed(InventoryErrorCode.INSUFFICIENT_STOCK.name());

        assertEquals(InventoryIdempotencyStatus.FAILED, inventoryIdempotency.getStatus());
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
}
