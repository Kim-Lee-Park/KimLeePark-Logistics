package com.klp.logistics.payment.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.klp.logistics.order.application.service.OrderService;
import com.klp.logistics.order.application.service.dto.OrderCreateCommand;
import com.klp.logistics.order.application.service.dto.OrderCreateCommand.Product;
import com.klp.logistics.order.domain.event.OrderCreatedEvent;
import com.klp.logistics.order.infrastructure.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PaymentEventListenerTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoSpyBean
    private PaymentEventListener paymentEventListener;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
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

        verify(paymentEventListener, times(1)).pay(any(OrderCreatedEvent.class));
    }
}
