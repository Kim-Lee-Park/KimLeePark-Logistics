package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.infrastructure.event.event.InventoryDeductedEvent;
import com.klp.order.infrastructure.event.event.InventoryDeductedFailedEvent;
import com.klp.order.infrastructure.event.event.InventoryReplenishedEvent;
import com.klp.order.infrastructure.event.event.InventoryReplenishedFailedEvent;
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

public class InventoryEventListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @KafkaListener(
        topics = "inventory.deducted",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryDeducted(
        @Payload InventoryDeductedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 재고 처리 이벤트 수신: orderId={}, partition={}, offset={} ===",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}", order.getOrderId());

            if (order.getOrderStatus() == OrderStatus.STOCK_CONFIRMED) {
                log.info("이미 처리된 재고 차감 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.STOCK_CONFIRMED);
            orderRepository.save(order);
            log.info("=== 재고 처리 이벤트 처리 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("재고 처리 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "inventory.deducted.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryDeductedFailed(
        @Payload InventoryDeductedFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 재고 확정 실패 이벤트 수신: orderId={}, partition={}, offset={} ===",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}", order.getOrderId());

            if (order.getOrderStatus() == OrderStatus.FAILED
                || order.getOrderStatus() == OrderStatus.STOCK_CONFIRMED_FAILED) {
                log.info("이미 처리된 재고 확정 실패 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.STOCK_CONFIRMED_FAILED);
            orderRepository.save(order);
            log.info("=== 재고 확정 실패 이벤트 처리 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("재고 확정 실패 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "inventory.replenished",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryReplenished(
        @Payload InventoryReplenishedEvent event,
        Acknowledgment acknowledgment) {

        log.info("=== 재고 증감 이벤트 수신: orderId={} ===", event.orderId());

        try {
            Order order = orderService.findById(event.orderId());

            if (order.getOrderStatus() != OrderStatus.CANCELLED) {
                log.warn("주문 취소 상태가 아닌데 재고 증감 이벤트 수신: orderId={}, status={}",
                    event.orderId(), order.getOrderStatus());
            }

            log.info("재고 증감 확인 완료: orderId={}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("재고 증감 이벤트 처리 실패: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "inventory.replenished.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryReplenishedFailed(
        @Payload InventoryReplenishedFailedEvent event,
        Acknowledgment acknowledgment) {

        log.error("=== 재고 증감 실패 이벤트 수신: orderId={} ===", event.orderId());

        try {
            Order order = orderService.findById(event.orderId());

            log.error("⚠️재고 증감 실패 - 수동 처리 필요: orderId={}",
                event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("재고 증감 실패 이벤트 처리 중 오류: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "inventory.deducted.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryDeductedDlt(InventoryDeductedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: InventoryDeducted");
        log.error("⚠️ 재고 차감 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, couponId={}", event.orderId(), event.userCouponId());
    }

    @KafkaListener(
        topics = "inventory.deducted.failed.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryDeductedFailedDlt(InventoryDeductedFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: InventoryDeductedFailed");
        log.error("⚠️ 재고 차감 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, couponId={}", event.orderId(), event.userCouponId());
    }

    @KafkaListener(
        topics = "inventory.replenished.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryReplenishedDlt(InventoryReplenishedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: InventoryReplenished");
        log.error("⚠️ 재고 복구 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, couponId={}", event.orderId(), event.userCouponId());
    }
}