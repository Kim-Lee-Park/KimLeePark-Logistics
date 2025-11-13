package com.klp.order.payment.domain.event;

import static org.junit.jupiter.api.Assertions.*;

import com.klp.order.payment.domain.event.PaymentCompletedEvent.PaidInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentCompletedEventTest {

    private UUID paymentId = UUID.randomUUID();

    private UUID productId = UUID.randomUUID();

    private List<PaidInfo> paidInfoList = List.of(
        new PaidInfo(productId, 1, BigDecimal.ZERO)
    );

    @Test
    @DisplayName("결제 정보 목록은 비어있을 수 없다")
    void emptyPaidInfos() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentCompletedEvent(paymentId, BigDecimal.ZERO, List.of())
        );
    }

    @Test
    @DisplayName("결제 정보의 결제 ID 는 Null 일 수 없다")
    void paymentIdIsNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentCompletedEvent(null, BigDecimal.ZERO, paidInfoList)
        );
    }

    @Test
    @DisplayName("총 결제금액은 Null 일 수 없다")
    void totalAmountIsNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentCompletedEvent(paymentId, null, paidInfoList)
        );
    }

    @Test
    @DisplayName("총 결제금액은 음수일 수 없습니다.")
    void totalAmountIsNotNegative() {
        BigDecimal totalAmount = BigDecimal.valueOf(-1);

        assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentCompletedEvent(paymentId, totalAmount, paidInfoList)
        );
    }
}
