package com.klp.order.order.application.event;

import com.klp.order.order.application.cache.ProductCache;
import com.klp.order.order.infrastructure.event.dto.ProductInfoChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInfoChangedHandler {

    private final ProductCache productCache;

    public void handle(ProductInfoChangedMessage msg) {
        switch (msg.eventType()) {
            case "PRODUCT_UPDATED" -> productCache.evictProduct(msg.productId());
            default -> log.warn(
                "[ProductInfoChangedHandler] Unknown eventType received: {} for productId={}",
                msg.eventType(), msg.productId());
        }
    }
}
