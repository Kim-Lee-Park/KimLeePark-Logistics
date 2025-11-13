package com.klp.order.payment.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentTest {

    private UUID orderId = UUID.randomUUID();

    @Test
    @DisplayName("amount 가 null 이라면 예외가 발생한다")
    void amount_is_null() {
        assertThrows(IllegalArgumentException.class, () -> new Payment(orderId, null));
    }

    @Test
    @DisplayName("amount 는 0 이상의 값이 아니라면 예외가 발생한다")
    void amount_is_negative() {
        assertThrows(IllegalArgumentException.class, () -> payment(BigDecimal.valueOf(-1)));
    }

    @Test
    @DisplayName("주문 ID 가 Null 이면 예외가 발생한다")
    void orderId_is_null() {
        assertThrows(IllegalArgumentException.class, () -> paymentByOrderId(null));
    }

    private Payment payment(BigDecimal amount) {
        return new Payment(orderId, amount);
    }

    private Payment paymentByOrderId(UUID orderId) {
        return new Payment(orderId, BigDecimal.ZERO);
    }
}
