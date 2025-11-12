package com.klp.logistics.order.application.service;

import com.klp.logistics.order.application.service.dto.OrderCreateCommand;
import com.klp.logistics.order.application.service.dto.OrderResponse;
import com.klp.logistics.order.domain.entity.order.Order;
import com.klp.logistics.order.domain.event.OrderCreatedEvent;
import com.klp.logistics.order.infrastructure.repository.OrderRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(OrderCreateCommand command) {
        Order order = Order.create(
            command.supplierId(),
            command.customerId(),
            command.comments(),
            command.toOrderItemCommands()
        );

        if (command.products().isEmpty()) {
            throw new RuntimeException();
        }

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(
            savedOrder.getOrderId(),
            command.products().stream().map(product -> new OrderCreatedEvent.Product(
                product.productId(),
                product.quantity(),
                product.price()
            )).toList(),
            LocalDateTime.now()
        ));

        return new OrderResponse();
    }
}
