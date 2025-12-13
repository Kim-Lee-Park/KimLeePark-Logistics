package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.infrastructure.event.event.CouponUsedEvent;
import com.klp.order.infrastructure.event.event.CouponUsedFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaHandler;
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
@KafkaListener(
    topics = "coupon.topic",
    groupId = "order-service-group",
    containerFactory = "kafkaListenerContainerFactory"
)
public class CouponEventListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @KafkaHandler
    @Transactional
    public void handleCouponUsed(
        @Payload CouponUsedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 쿠폰 사용 완료 이벤트 수신: orderId={}, couponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);

        try {
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
            log.info("=== 쿠폰 사용 완료 이벤트 처리 완료: orderId={}, couponId={} ===",
                event.orderId(), event.userCouponId());

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

    @KafkaHandler
    @Transactional
    public void handleCouponUsedFailed(
        @Payload CouponUsedFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 쿠폰 사용 실패 이벤트 수신: orderId={}, couponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}", order.getOrderId());

            if (order.getOrderStatus() == OrderStatus.FAILED
                || order.getOrderStatus() == OrderStatus.COUPON_CONFIRMED_FAILED) {
                log.info("이미 처리된 쿠폰 사용 실패 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.COUPON_CONFIRMED_FAILED);
            orderRepository.save(order);
            log.info("=== 쿠폰 사용 실패 이벤트 처리 완료: orderId={}, couponId={} ===",
                event.orderId(), event.userCouponId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("쿠폰 사용 실패 이벤트 처리 실패: orderId={}, couponId={}, partition={}, offset={}",
                event.orderId(), event.userCouponId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }

    @DltHandler
    public void handleCouponDlt(
        @Payload Object event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("========================================");
        log.error("⚠️ DLT 도착: Coupon Event");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("eventType={}, error={}", event.getClass().getSimpleName(), exceptionMessage);
    }
}
