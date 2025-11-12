package com.klp.order.domain.event;

import static org.junit.jupiter.api.Assertions.*;

import com.klp.order.domain.event.OrderCreatedEvent.Product;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderCreatedEventTest {

    @Test
    @DisplayName("상품이 빈값이면 예외가 발생한다")
    void throwEmptyProducts() {
        assertThrows(IllegalArgumentException.class, () -> new OrderCreatedEvent(
            UUID.randomUUID(),
            List.of(),
            LocalDateTime.now()
        ));
    }

    @Test
    @DisplayName("상품이 Null 이라면 예외가 발생한다")
    void throwNullProducts() {
        assertThrows(IllegalArgumentException.class, () -> new OrderCreatedEvent(
            UUID.randomUUID(),
            null,
            LocalDateTime.now()
        ));
    }

    @Test
    @DisplayName("주문 ID는 필수값입니다.")
    void orderIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new OrderCreatedEvent(
            null,
            List.of(new Product(UUID.randomUUID(), 10, 10)),
            LocalDateTime.now()
        ));
    }
}
