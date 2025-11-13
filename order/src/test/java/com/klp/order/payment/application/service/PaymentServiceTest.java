package com.klp.order.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.order.payment.application.service.dto.PaymentCommand;
import com.klp.order.payment.application.service.dto.PaymentCommand.PaymentInfo;
import com.klp.order.payment.domain.entity.Payment;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.order.payment.infrastructure.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ExternalPaymentClient externalPaymentClient;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId = UUID.randomUUID();

    private UUID productId = UUID.randomUUID();

    @Test
    @DisplayName("외부 결제 API 를 호출한다")
    void callExternalPayment() {
        PaymentCommand command = new PaymentCommand(orderId, List.of(new PaymentInfo(productId, 10, BigDecimal.ZERO)));
        Payment payment = mock(Payment.class);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(payment.getPaymentId()).thenReturn(UUID.randomUUID());

        paymentService.pay(command);

        verify(externalPaymentClient, times(1)).payment();
    }
}
