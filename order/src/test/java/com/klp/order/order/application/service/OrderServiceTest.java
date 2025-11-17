package com.klp.order.order.application.service;

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
import com.klp.order.order.application.service.dto.OrderCreateCommand;
import com.klp.order.order.application.service.dto.OrderCreateCommand.Product;
import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.domain.event.OrderCreatedEvent;
import com.klp.order.order.infrastructure.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private EventStoreService eventStoreService;

    @InjectMocks
    private OrderService orderService;

    private Long supplierId = 1L;
    private Long customerId = 1L;
    private OrderCreateCommand.Product product = new Product(
        UUID.randomUUID(),
        10,
        10
    );
    private String comments = "comments";

    @Test
    @DisplayName("주문을 생성하면 OrderCreatedEvent 를 발행한다")
    void publishOrderCreatedEvent() {
        OrderCreateCommand command = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(product),
            comments
        );
        Order order = mock(Order.class);
        when(orderRepository.save(any())).thenReturn(order);
        when(order.getOrderId()).thenReturn(UUID.randomUUID());

        orderService.createOrder(command);

        verify(eventPublisher, times(1)).publish(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("주문 생성에 성공하면 OrderCreatedEvent 를 이벤트 스토어에 저장한다")
    void saveEvent() {
        OrderCreateCommand command = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(product),
            comments
        );
        Order order = mock(Order.class);
        when(orderRepository.save(any())).thenReturn(order);
        when(order.getOrderId()).thenReturn(UUID.randomUUID());

        orderService.createOrder(command);

        verify(eventStoreService, times(1)).saveEvent(any(), eq(EventType.ORDER_CREATED), any());
    }

    @Test
    @DisplayName("주문 생성에 실패하면 OrderCreatedEvent 를 발행하지 않는다")
    void verifyNever() {
        OrderCreateCommand invalidCommand = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(),
            comments
        );

        assertThatThrownBy(
            () -> orderService.createOrder(invalidCommand)
        ).isInstanceOf(RuntimeException.class);
        verify(eventPublisher, never()).publish(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("주문 생성에 실패하면 OrderCreatedEvent 를 이벤트 스토어에 저장하지 않는다")
    void eventStoreVerifyNever() {
        OrderCreateCommand invalidCommand = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(),
            comments
        );

        assertThatThrownBy(
            () -> orderService.createOrder(invalidCommand)
        ).isInstanceOf(RuntimeException.class);
        verify(eventStoreService, never()).saveEvent(any(), any(), any());
    }

    @Test
    @DisplayName("상품이 존재하지 않는다면 예외가 발생한다")
    void throwEmptyProducts() {
        OrderCreateCommand invalidCommand = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(),
            comments
        );

        assertThatThrownBy(
            () -> orderService.createOrder(invalidCommand)
        ).isInstanceOf(RuntimeException.class);
    }
}
