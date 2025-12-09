package com.klp.order.infrastructure.event.event;

import java.util.List;
import java.util.UUID;

/*
 * 이 event만 일단 두겠습니다. 명진님!
 * 지금 이거를 다른것과 동일하게 바꾸면 빨간불이 많이 뜨는데 커버하기 힘들거 같습니다..
 * */
public record DeliveryCreatedEvent(
    UUID orderId,
    List<DeliveryItem> items
) {

    public record DeliveryItem(
        UUID orderItemId,
        UUID deliveryId
    ) {

    }
}
