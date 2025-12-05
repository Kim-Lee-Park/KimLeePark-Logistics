package com.klp.ai.recommendation.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCompletedEvent(
    UUID orderId,
    UUID paymentId,
    Long amount,
    String status,
    LocalDateTime paidAt

) {

}
