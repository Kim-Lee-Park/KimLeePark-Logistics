package com.klp.order.infrastructure.event;

import java.util.UUID;

// 아직 결제 쪽이 구현이 안되어있기 때문에 추후에 코드 수정하겠습니다.
public record PaymentCompletedEvent(
    UUID orderId,
    UUID paymentId
) {

}