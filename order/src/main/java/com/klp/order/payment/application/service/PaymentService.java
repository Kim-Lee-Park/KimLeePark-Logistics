package com.klp.order.payment.application.service;

import com.klp.order.common.event.EventPublisher;
import com.klp.order.common.event.EventStoreService;
import com.klp.order.common.event.domain.EventType;
import com.klp.order.payment.application.service.dto.PaymentCreateCommand;
import com.klp.order.payment.domain.entity.Payment;
import com.klp.order.payment.domain.event.PaymentCompletedEvent;
import com.klp.order.payment.domain.event.PaymentCompletedEvent.PaidInfo;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.order.payment.infrastructure.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ExternalPaymentClient externalPaymentClient;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final EventStoreService eventStoreService;

    @Transactional
    public UUID pay(PaymentCreateCommand command) {
        BigDecimal totalAmount = command.totalAmount();
        Payment payment = new Payment(command.orderId(), totalAmount);

        // 외부 API 호출
        externalPaymentClient.payment();

        payment.completed();
        Payment savedPayment = paymentRepository.save(payment);

        PaymentCompletedEvent event = new PaymentCompletedEvent(
            command.orderId(),
            savedPayment.getPaymentId(),
            totalAmount,
            command.infos().stream().map(info -> new PaidInfo(
                info.productId(),
                info.quantity(),
                info.amount()
            )).toList(),
            LocalDateTime.now()
        );
        eventPublisher.publish(event);
        eventStoreService.saveEvent(event, EventType.PAYMENT_COMPLETED, event.occurredAt());

        return savedPayment.getPaymentId();
    }
}
