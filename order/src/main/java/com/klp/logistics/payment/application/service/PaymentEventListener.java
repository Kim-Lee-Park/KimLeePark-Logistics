package com.klp.logistics.payment.application.service;

import com.klp.logistics.order.domain.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
public class PaymentEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pay(OrderCreatedEvent event) {
        log.info("결제 처리 시작 order ID : {}", event.orderId());
    }
}
