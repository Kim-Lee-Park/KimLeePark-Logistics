package com.klp.order.payment.application.service;

import com.klp.order.payment.application.service.dto.PaymentCommand;
import com.klp.order.payment.domain.entity.Payment;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.order.payment.infrastructure.repository.PaymentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ExternalPaymentClient externalPaymentClient;

    private final PaymentRepository paymentRepository;

    @Transactional
    public UUID pay(PaymentCommand command) {
        Payment payment = new Payment(command.orderId(), command.amount());

        externalPaymentClient.payment();

        payment.completed();

        return paymentRepository.save(payment).getPaymentId();
    }
}
