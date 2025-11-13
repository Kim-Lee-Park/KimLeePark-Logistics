package com.klp.order.payment.application.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.order.payment.application.service.dto.PaymentCreateCommand.PaymentInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentCreateCommandTest {
    private UUID orderId = UUID.randomUUID();

    private UUID productId1 = UUID.randomUUID();
    private UUID productId2 = UUID.randomUUID();

    @Test
    @DisplayName("총 가격을 계산한다")
    void totalAmount() {
        Integer quantity1 = 10;
        Integer quantity2 = 20;
        BigDecimal price1 = BigDecimal.valueOf(100);
        BigDecimal price2 = BigDecimal.valueOf(200);
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            List.of(
                new PaymentInfo(productId1, quantity1, price1),
                new PaymentInfo(productId2, quantity2, price2)
            )
        );

        BigDecimal result = command.totalAmount();

        // (10 * 100) + (20 * 200)
        assertThat(result).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("각 상품의 가격을 계산할 수 있다")
    void eachProductTotalAmount() {
        Integer quantity1 = 10;
        Integer quantity2 = 20;
        BigDecimal price1 = BigDecimal.valueOf(100);
        BigDecimal price2 = BigDecimal.valueOf(200);
        PaymentInfo paymentInfo1 = new PaymentInfo(productId1, quantity1, price1);
        PaymentInfo paymentInfo2 = new PaymentInfo(productId2, quantity2, price2);

        BigDecimal result1 = paymentInfo1.totalAmount();
        BigDecimal result2 = paymentInfo2.totalAmount();

        // 10 * 100
        assertThat(result1).isEqualTo(BigDecimal.valueOf(1000));
        // 20 * 200
        assertThat(result2).isEqualTo(BigDecimal.valueOf(4000));
    }
}
