package com.klp.order.payment.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.order.common.event.EventPublisher;
import com.klp.order.common.event.EventStoreService;
import com.klp.order.common.event.domain.EventType;
import com.klp.order.payment.application.service.dto.PaymentCreateCommand;
import com.klp.order.payment.application.service.dto.PaymentCreateCommand.PaymentInfo;
import com.klp.order.payment.domain.entity.Payment;
import com.klp.order.payment.domain.event.PaymentCompletedEvent;
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
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ExternalPaymentClient externalPaymentClient;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private EventStoreService eventStoreService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId = UUID.randomUUID();

    private UUID productId = UUID.randomUUID();

    private List<PaymentCreateCommand.PaymentInfo> paymentInfoList = List.of(
        new PaymentCreateCommand.PaymentInfo(
            productId,
            1,
            BigDecimal.ZERO
        )
    );

    @Test
    @DisplayName("외부 결제 API 를 호출한다")
    void callExternalPayment() {
        PaymentCreateCommand command = new PaymentCreateCommand(orderId, List.of(new PaymentInfo(productId, 10, BigDecimal.ZERO)));
        Payment payment = mock(Payment.class);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(payment.getPaymentId()).thenReturn(UUID.randomUUID());

        paymentService.pay(command);

        verify(externalPaymentClient, times(1)).payment();
    }

    @Test
    @DisplayName("결제를 성공하면 PaymentCompletedEvent 를 발행한다")
    void publishPaymentCompletedEvent() {
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            paymentInfoList
        );
        Payment payment = mock(Payment.class);
        when(paymentRepository.save(any())).thenReturn(payment);
        when(payment.getPaymentId()).thenReturn(UUID.randomUUID());

        paymentService.pay(command);

        verify(eventPublisher, times(1)).publish(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("결제를 성공하면 PaymentCompletedEvent 를 이벤트 스토어에 저장한다")
    void saveEvent() {
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            paymentInfoList
        );
        Payment payment = mock(Payment.class);
        when(paymentRepository.save(any())).thenReturn(payment);
        when(payment.getPaymentId()).thenReturn(UUID.randomUUID());

        paymentService.pay(command);

        verify(eventStoreService, times(1)).saveEvent(any(), any(), any());
    }

    @Test
    @DisplayName("결제에 실패하면 PaymentCompletedEvent 를 발행하지 않는다")
    void verifyNever() {
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            paymentInfoList
        );
        when(externalPaymentClient.payment())
            .thenThrow(RuntimeException.class);

        assertThatThrownBy(
            () -> paymentService.pay(command)
        ).isInstanceOf(RuntimeException.class);
        verify(eventPublisher, never()).publish(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("결제에 실패하면 PaymentCompletedEvent 를 이벤트 스토어에 저장하지 않는다")
    void eventStoreVerifyNever() {
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            paymentInfoList
        );
        when(externalPaymentClient.payment())
            .thenThrow(RuntimeException.class);

        assertThatThrownBy(
            () -> paymentService.pay(command)
        ).isInstanceOf(RuntimeException.class);
        verify(eventStoreService, never()).saveEvent(any(), eq(EventType.PAYMENT_COMPLETED), any());
    }
}
