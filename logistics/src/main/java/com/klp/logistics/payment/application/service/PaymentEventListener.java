package com.klp.logistics.payment.application.service;

import com.klp.logistics.order.domain.event.OrderCreatedEvent;
import com.klp.logistics.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.logistics.util.PerformanceMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventListener {

    private final ExternalPaymentClient paymentClient;
    private final PerformanceMonitor performanceMonitor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pay(OrderCreatedEvent event) {
        performanceMonitor.measure("PaymentEventListener.payment", () -> {
            log.info("결제 처리 시작 order ID : {}", event.orderId());
            paymentClient.payment();
            log.info("결제 처리 성공");
        });
    }
}
