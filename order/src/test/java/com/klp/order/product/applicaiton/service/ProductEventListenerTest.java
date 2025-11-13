package com.klp.order.product.applicaiton.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.klp.order.order.infrastructure.repository.OrderRepository;
import com.klp.order.payment.application.service.PaymentService;
import com.klp.order.payment.application.service.dto.PaymentCreateCommand;
import com.klp.order.payment.application.service.dto.PaymentCreateCommand.PaymentInfo;
import com.klp.order.payment.domain.entity.Payment;
import com.klp.order.payment.domain.event.PaymentCompletedEvent;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.order.payment.infrastructure.repository.PaymentRepository;
import com.klp.order.product.domain.entity.Product;
import com.klp.order.product.infrastructure.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
class ProductEventListenerTest {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private ExternalPaymentClient externalPaymentClient;

    @MockitoSpyBean
    private ProductEventListener productEventListener;

    private UUID orderId = UUID.randomUUID();

    private UUID productId = UUID.randomUUID();

    private List<PaymentInfo> paymentInfos = List.of(
        new PaymentInfo(
            productId,
            1,
            BigDecimal.ZERO
        )
    );

    @BeforeEach
    void setUp() {
        clearInvocations(productEventListener);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        reset(productEventListener);

        await()
            .atMost(Duration.ofSeconds(1))
            .pollDelay(Duration.ofMillis(100))
            .until(() -> true);
    }

    @Test
    @DisplayName("결제 완료 이벤트를 구독하여 deduct() 를 수행할 수 있다")
    void subscribePaymentCompletedEvent() {
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            paymentInfos
        );

        paymentService.pay(command);

        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                verify(productEventListener, times(1))
                    .deduct(any(PaymentCompletedEvent.class));
            });
    }

    @Test
    @DisplayName("결제에 실패한 경우 재고 차감이 시도되지 않는다")
    void throwOrderCreatedEvent() {
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            paymentInfos
        );
        doThrow(new RuntimeException("결제 실패"))
            .when(externalPaymentClient).payment();

        assertThatThrownBy(
            () -> paymentService.pay(command)
        ).isInstanceOf(RuntimeException.class);
        verify(productEventListener, never()).deduct(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("이벤트가 비동기로 처리된다")
    void asyncEventHandle() {
        ArgumentCaptor<PaymentCompletedEvent> eventCaptor = ArgumentCaptor.forClass(
            PaymentCompletedEvent.class);
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            paymentInfos
        );

        paymentService.pay(command);

        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                verify(productEventListener, times(1)).deduct(eventCaptor.capture());
                PaymentCompletedEvent capturedEvent = eventCaptor.getValue();

                assertThat(capturedEvent.orderId()).isNotNull();
                assertThat(capturedEvent.paymentId()).isNotNull();
                assertThat(capturedEvent.paidInfos()).isNotEmpty();
                assertThat(capturedEvent.occurredAt()).isNotNull();
            });
    }

    @Test
    @DisplayName("재고 차감에 실패하여도 결제는 생성된다")
    void transactionPropagation() {
        Integer stock = 1;
        Product savedProduct = productRepository.save(
            new Product("상품명", stock)
        );
        PaymentCreateCommand command = new PaymentCreateCommand(
            orderId,
            List.of(
                new PaymentInfo(
                    savedProduct.getProductId(),
                    2,
                    BigDecimal.ZERO
                )
            )
        );

        paymentService.pay(command);

        Product product = productRepository.findById(savedProduct.getProductId()).orElseThrow();
        assertThat(product.getStock()).isEqualTo(stock);
        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).isNotEmpty();
    }
}
