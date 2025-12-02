package com.klp.order.infrastructure.event.listener;


import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.infrastructure.event.OrderEventPublisher;
import com.klp.order.infrastructure.event.OrderPaidEvent;
import com.klp.order.infrastructure.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderOutboundRequestService orderOutboundRequestService;
    private final OrderEventPublisher eventPublisher;

    // 결제 완료 이벤트를 받으면 배송 생성 요청 이벤트를 발행
    @KafkaListener(
        topics = "payment.completed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentCompleted(
        @Payload PaymentCompletedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 결제 완료 이벤트 수신: orderId={}, partition={}, offset={} ===",
            event.orderId(), partition, offset);

        try {
            // 1. 주문 조회 후 상태 변경
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}", order.getOrderId());

            order.changeStatus(OrderStatus.PAID);
            orderRepository.save(order);

            // 2. 배송 요청 이벤트 발행
            String deliveryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
                order.getOrderId(),
                Target.DELIVERY,
                OperationType.MAKING
            );

            OrderPaidEvent deliveryEvent = OrderPaidEvent.from(
                order,
                deliveryIdempotencyKey
            );
            eventPublisher.publishOrderPaid(deliveryEvent);

            // 3. 수동 커밋
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

            log.info("배송 요청 이벤트 발행 완료 - orderId: {}", order.getOrderId());
            log.info("=== 결제 완료 이벤트 처리 완료: orderId={} ===", event.orderId());

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }
}
