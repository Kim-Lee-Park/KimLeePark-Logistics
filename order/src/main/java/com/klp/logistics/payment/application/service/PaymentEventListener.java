package com.klp.logistics.payment.application.service;

import com.klp.logistics.order.domain.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentEventListener {

    @EventListener
    public void pay(OrderCreatedEvent event) {
        log.info("결제 처리 시작 order ID : {}", event.orderId());
    }
}
