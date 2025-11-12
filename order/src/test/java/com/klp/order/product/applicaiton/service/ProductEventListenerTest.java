package com.klp.order.product.applicaiton.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.klp.order.order.application.service.OrderService;
import com.klp.order.order.application.service.dto.OrderCreateCommand;
import com.klp.order.order.application.service.dto.OrderCreateCommand.Product;
import com.klp.order.order.domain.event.OrderCreatedEvent;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
class ProductEventListenerTest {
    @Autowired
    private OrderService orderService;

    @MockitoSpyBean
    private ProductEventListener productEventListener;

    private Long supplierId = 1L;
    private Long customerId = 1L;
    private OrderCreateCommand.Product product = new Product(
        UUID.randomUUID(),
        10,
        10
    );
    private String comments = "comments";

    @Test
    @DisplayName("주문 생성 이벤트를 구독하여 deduct() 를 수행할 수 있다")
    void subscribeOrderCreatedEvent() {
        OrderCreateCommand command = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(product),
            comments
        );

        orderService.createOrder(command);

        verify(productEventListener, times(1)).deduct(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("주문 생성에 실패한 경우 재고 차감이 시도되지 않는다")
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
        verify(productEventListener, never()).deduct(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("주문 생성에 성공한 경우 재고차감이 시도한다")
    void tryPaySuccess() {
        OrderCreateCommand command = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(product),
            comments
        );

        orderService.createOrder(command);

        verify(productEventListener, times(1)).deduct(any(OrderCreatedEvent.class));
    }
}
