package com.klp.order.product.applicaiton.service;

import com.klp.order.order.domain.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
public class ProductEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deduct(OrderCreatedEvent event) {
        log.info("재고 차감 order ID : {}", event.orderId());
    }
}
