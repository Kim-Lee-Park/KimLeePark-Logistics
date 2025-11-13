package com.klp.order.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

import com.klp.order.order.application.service.OrderService;
import com.klp.order.order.application.service.dto.OrderCreateCommand;
import com.klp.order.order.application.service.dto.OrderCreateCommand.Product;
import com.klp.order.order.application.service.dto.OrderResponse;
import com.klp.order.order.domain.event.OrderCreatedEvent;
import com.klp.order.order.infrastructure.repository.OrderRepository;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.order.product.applicaiton.service.ProductEventListener;
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
class PaymentEventListenerTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private ExternalPaymentClient externalPaymentClient;

    @MockitoSpyBean
    private PaymentEventListener paymentEventListener;

    @BeforeEach
    void setUp() {
        clearInvocations(paymentEventListener);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        reset(paymentEventListener);

        await()
            .atMost(Duration.ofSeconds(1))
            .pollDelay(Duration.ofMillis(100))
            .until(() -> true);
    }

    private Long supplierId = 1L;
    private Long customerId = 1L;
    private OrderCreateCommand.Product product = new Product(
        UUID.randomUUID(),
        10,
        10
    );
    private String comments = "comments";

    @Test
    @DisplayName("주문 생성 이벤트를 구독하여 pay() 메서드를 수행할 수 있다")
    void subscribeOrderCreatedEvent() {
        OrderCreateCommand command = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(product),
            comments
        );

        orderService.createOrder(command);

        verify(paymentEventListener, times(1)).pay(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("주문 생성에 실패한 경우 결제가 시도되지 않는다")
    void throwOrderCreatedEvent() {
        OrderCreateCommand invalidCommand = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(),
            comments
        );

        assertThatThrownBy(
            () -> orderService.createOrder(invalidCommand)
        ).isInstanceOf(RuntimeException.class);
        verify(paymentEventListener, never()).pay(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("주문 생성에 성공한 경우 결제를 시도한다")
    void tryPaySuccess() {
        OrderCreateCommand command = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(product),
            comments
        );

        orderService.createOrder(command);

        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                verify(paymentEventListener, times(1))
                    .pay(any(OrderCreatedEvent.class));
            });
    }

    @Test
    @DisplayName("이벤트가 비동기로 처리된다")
    void asyncEventHandle() {
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        OrderCreateCommand command = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(product),
            comments
        );

        orderService.createOrder(command);

        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                verify(paymentEventListener, times(1)).pay(eventCaptor.capture());
                OrderCreatedEvent capturedEvent = eventCaptor.getValue();

                assertThat(capturedEvent.orderId()).isNotNull();
                assertThat(capturedEvent.products()).isNotNull();
                assertThat(capturedEvent.products()).isNotEmpty();
                assertThat(capturedEvent.occurredAt()).isNotNull();
            });
    }

    @Test
    @DisplayName("비동기로 실행되는 이벤트 로직의 예외가 메인 스레드에 영향을 주지 않는다")
    void nonSideEffect() {
        OrderCreateCommand command = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(product),
            comments
        );
        doThrow(new RuntimeException("결제 실패"))
            .when(externalPaymentClient).payment();

        OrderResponse response = orderService.createOrder(command);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isNotNull();
    }
}
