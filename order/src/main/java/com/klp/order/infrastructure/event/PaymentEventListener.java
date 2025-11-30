package com.klp.order.infrastructure.event;


import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;
    private final OrderOutboundRequestService orderOutboundRequestService;
    private final OrderEventPublisher eventPublisher;

    /**
     * Payment 완료 이벤트를 받으면 Delivery 요청 이벤트 발행
     */
    @KafkaListener(
        topics = "payment.completed",
        groupId = "order-service-group"
    )
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("=== 결제 완료 이벤트 수신: orderId={} ===", event.orderId());

        try {
            // 1. 주문 조회
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}", order.getOrderId());

            // 2. 배송 요청 이벤트 발행 (Delivery로)
            String deliveryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
                order.getOrderId(),
                Target.DELIVERY,
                OperationType.MAKING
            );

            OrderDeliveryRequestEvent deliveryEvent = OrderDeliveryRequestEvent.from(
                order,
                deliveryIdempotencyKey
            );
            eventPublisher.publishDeliveryRequest(deliveryEvent);

            log.info("배송 요청 이벤트 발행 완료 - orderId: {}", order.getOrderId());
            log.info("=== 결제 완료 이벤트 처리 완료: orderId={} ===", event.orderId());

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: orderId={}", event.orderId(), e);
            throw e; // DLQ로 보내기 위해 예외 재발생
        }
    }

    public record PaymentCompletedEvent(
        UUID orderId,
        UUID paymentId
    ) {

    }
}
