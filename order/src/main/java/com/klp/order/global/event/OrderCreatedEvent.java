package com.klp.order.global.event;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCreatedEvent {

    private final UUID orderId;
    private final UUID productId;
    private final int quantity;
    private final LocalDateTime occurredAt;

//    public static OrderCreatedEvent of(Order order) {
//        return new OrderCreatedEvent(
//            order.getOrderId(),
////            order.getProductId(),
////            order.getQuantity(),
//            LocalDateTime.now()
//        );
//    }
}
