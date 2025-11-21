package com.klp.payment.application.service;

import com.klp.payment.application.service.dto.PaymentCreateCommand;
import com.klp.payment.domain.entity.Payment;
import com.klp.payment.infrastructure.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public UUID pay(PaymentCreateCommand command) {
        log.info("결제 처리");
        BigDecimal totalAmount = command.totalAmount();
        Payment payment = new Payment(command.orderId(), totalAmount);

        payment.completed();
        Payment savedPayment = paymentRepository.save(payment);

        log.info("결제 성공");
        return savedPayment.getPaymentId();
    }
}
