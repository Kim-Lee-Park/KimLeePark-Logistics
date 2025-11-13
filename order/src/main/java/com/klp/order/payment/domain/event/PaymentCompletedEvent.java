package com.klp.order.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentCompletedEvent(
    UUID paymentId,
    UUID orderId,
    BigDecimal totalAmount,
    List<PaidInfo> paidInfos,
    LocalDateTime occurredAt
) {
   public PaymentCompletedEvent {
      if (orderId == null) {
         throw new IllegalArgumentException("주문 ID 는 필수값입니다.");
      }

      if (paymentId == null) {
         throw new IllegalArgumentException("결제 ID 는 필수값입니다.");
      }

      if (totalAmount == null) {
         throw new IllegalArgumentException("총 결제금액은 필수값입니다.");
      }

      if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
         throw new IllegalArgumentException("총 결제금액은 음수일 수 없습니다.");
      }

      if (paidInfos.isEmpty()) {
         throw new IllegalArgumentException("결제 정보 목록은 비어있을 수 없습니다.");
      }
   }

   public record PaidInfo(
       UUID productId,
       Integer quantity,
       BigDecimal amount
   ) {}
}
