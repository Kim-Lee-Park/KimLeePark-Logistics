package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.infrastructure.event.event.CouponUsedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsedEventListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000L, multiplier = 2.0, maxDelay = 4000L),
        autoCreateTopics = "true",
        include = Exception.class,
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
        topics = "coupon.topic",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCoupon(
        @Payload CouponUsedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 쿠폰 사용 완료 이벤트 수신: orderId={},couponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);

        try {
            // 1. 주문 조회 후 상태 변경
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}", order.getOrderId());

            if (order.getOrderStatus() == OrderStatus.COMPLETE
                || order.getOrderStatus() == OrderStatus.COUPON_CONFIRMED) {
                log.info("이미 처리된 쿠폰 사용 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.COUPON_CONFIRMED);
            orderRepository.save(order);
            log.info("=== 쿠폰 사용 완료 이벤트 처리 완료: orderId={}, couponId={} ===", event.orderId(),
                event.userCouponId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("쿠폰 사용 완료 이벤트 처리 실패: orderId={}, couponId={}, partition={}, offset={}",
                event.orderId(), event.userCouponId(), partition, offset, e);
            throw e;
        }
    }

    // 실패시 자동으로 호출한다고 합니다.
    @DltHandler
    public void handleCouponUsedDlt(
        @Payload CouponUsedEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("========================================");
        log.error("⚠️ DLT 도착: Coupon Used Event");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("orderId={}, error={}", event.orderId(), exceptionMessage);
    }

}
