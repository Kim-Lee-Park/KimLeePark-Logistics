package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.infrastructure.event.event.CouponCancelledEvent;
import com.klp.order.infrastructure.event.event.CouponCancelledFailedEvent;
import com.klp.order.infrastructure.event.event.CouponUsedEvent;
import com.klp.order.infrastructure.event.event.CouponUsedFailedEvent;
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
public class CouponEventListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @KafkaListener(
        topics = "coupon.used",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
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
            log.info("=== 쿠폰 사용 완료 이벤트 처리 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("쿠폰 사용 완료 이벤트 처리 실패: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "coupon.used.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCouponUsedFailed(
        @Payload CouponUsedFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 쿠폰 사용 실패 이벤트 수신: orderId={} ===", event.orderId());

        try {
            Order order = orderService.findById(event.orderId());

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
            log.info("=== 쿠폰 사용 실패 이벤트 처리 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("쿠폰 사용 실패 이벤트 처리 실패: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "coupon.restored",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCouponCancelled(
        @Payload CouponCancelledEvent event,
        Acknowledgment acknowledgment) {

        log.info("=== 쿠폰 복원 완료 이벤트 수신: orderId={} ===", event.orderId());

        try {
            Order order = orderService.findById(event.orderId());

            if (order.getOrderStatus() != OrderStatus.CANCELLED) {
                log.warn("주문 취소 상태가 아닌데 쿠폰 복원 이벤트 수신: orderId={}, status={}",
                    event.orderId(), order.getOrderStatus());
            }

            log.info("쿠폰 복원 확인 완료: orderId={}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("쿠폰 복원 이벤트 처리 실패: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "coupon.restored.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCouponCancelledFailed(
        @Payload CouponCancelledFailedEvent event,
        Acknowledgment acknowledgment) {

        log.error("=== 쿠폰 복원 실패 이벤트 수신: orderId={} ===", event.orderId());

        try {
            Order order = orderService.findById(event.orderId());
            log.error("⚠️ 쿠폰 복원 실패 - 수동 처리 필요: orderId={}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("쿠폰 복원 실패 이벤트 처리 중 오류: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    // ========== DLT 처리 (별도 그룹) ==========

    @KafkaListener(
        topics = "coupon.used.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCouponUsedDlt(CouponUsedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: CouponUsed");
        log.error("⚠️ 쿠폰 사용 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, couponId={}", event.orderId(), event.userCouponId());
    }

    @KafkaListener(
        topics = "coupon.used.failed.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCouponUsedFailedDlt(CouponUsedFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: CouponUsedFailed");
        log.error("⚠️ 쿠폰 사용 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, couponId={}", event.orderId(), event.userCouponId());
    }

    @KafkaListener(
        topics = "coupon.restored.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCouponCancelledDlt(CouponCancelledEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: CouponRestored");
        log.error("⚠️ 쿠폰 복원 확인 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = "coupon.restored.failed.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCouponCancelledFailedDlt(CouponCancelledFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: CouponRestoredFailed");
        log.error("⚠️ 쿠폰 복원 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }
}