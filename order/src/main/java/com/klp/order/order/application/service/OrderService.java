package com.klp.order.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.common.event.EventPublisher;
import com.klp.order.order.application.service.dto.OrderCreateCommand;
import com.klp.order.order.application.service.dto.OrderResponse;
import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.outbox.domain.entity.EventType;
import com.klp.order.outbox.domain.entity.OutboxEvent;
import com.klp.order.order.domain.event.OrderCreatedEvent;
import com.klp.order.order.domain.event.OrderCreatedEvent.Product;
import com.klp.order.order.infrastructure.repository.OrderRepository;
import com.klp.order.outbox.infrastructure.repository.OutboxRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    @Transactional
    public OrderResponse createOrder(OrderCreateCommand command) {
        log.info("주문 생성 처리");
        Order order = Order.create(
            command.supplierId(),
            command.customerId(),
            command.comments(),
            command.toOrderItemCommands()
        );

        if (command.products().isEmpty()) {
            log.info("상품이 존재하지 않습니다.");
            throw new RuntimeException("상품이 존재하지 않습니다.");
        }

        Order savedOrder = orderRepository.save(order);

        log.info("주문 생성 완료 이벤트 발행");
        OrderCreatedEvent event = new OrderCreatedEvent(
            savedOrder.getOrderId(),
            command.products().stream().map(product -> new Product(
                product.productId(),
                product.quantity(),
                product.price()
            )).toList(),
            LocalDateTime.now()
        );
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxEvent(EventType.ORDER_CREATED, payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        eventPublisher.publish(event);

        log.info("주문 생성 완료");
        return new OrderResponse(savedOrder.getOrderId());
    }
}
