package com.klp.logistics.product.applicaiton.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.klp.logistics.order.application.service.OrderService;
import com.klp.logistics.order.application.service.dto.OrderCreateCommand;
import com.klp.logistics.order.application.service.dto.OrderCreateCommand.Product;
import com.klp.logistics.order.domain.event.OrderCreatedEvent;
import com.klp.logistics.payment.application.service.PaymentEventListener;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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
}
