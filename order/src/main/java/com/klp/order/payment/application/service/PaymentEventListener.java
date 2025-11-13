package com.klp.order.payment.application.service;

import com.klp.order.order.domain.event.OrderCreatedEvent;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventListener {

    private final ExternalPaymentClient paymentClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pay(OrderCreatedEvent event) {
        log.info("결제 처리 시작 order ID : {}", event.orderId());
        log.info("Thread name : {}", Thread.currentThread().getName());
        paymentClient.payment();
        log.info("결제 처리 성공");
    }
}
