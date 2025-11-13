package com.klp.order.payment.application.service;

import com.klp.order.payment.application.service.dto.PaymentCommand;
import com.klp.order.payment.domain.entity.Payment;
import com.klp.order.payment.domain.event.PaymentCompletedEvent;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.order.payment.infrastructure.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ExternalPaymentClient externalPaymentClient;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID pay(PaymentCommand command) {
        BigDecimal totalAmount = command.totalAmount();
        Payment payment = new Payment(command.orderId(), totalAmount);

        // 외부 API 호출
        externalPaymentClient.payment();

        payment.completed();
        Payment savedPayment = paymentRepository.save(payment);

        eventPublisher.publishEvent(
            new PaymentCompletedEvent(
                command.orderId(),
                savedPayment.getPaymentId(),
                totalAmount,
                command.infos().stream().map(info -> new PaymentCompletedEvent.PaidInfo(
                    info.productId(),
                    info.quantity(),
                    info.amount()
                )).toList(),
                LocalDateTime.now()
            )
        );

        return savedPayment.getPaymentId();
    }
}
