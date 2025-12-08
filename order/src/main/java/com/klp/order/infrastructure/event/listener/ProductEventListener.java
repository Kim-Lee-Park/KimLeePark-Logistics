package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.event.ProductInfoChangedHandler;
import com.klp.order.infrastructure.event.dto.ProductInfoChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final ProductInfoChangedHandler productInfoChangedHandler;

    @KafkaListener(
        topics = "product.info.changed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProductInfoChanged(ProductInfoChangedMessage msg) {
        log.info("[ProductEventListener] user.profile.changed received. productId={}, eventType={}",
            msg.productId(), msg.eventType());
        productInfoChangedHandler.handle(msg);
    }
}
