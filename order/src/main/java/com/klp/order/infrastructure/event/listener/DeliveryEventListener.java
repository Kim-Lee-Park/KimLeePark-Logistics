package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.service.OrderItemService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.infrastructure.event.event.DeliveryArrivedEvent;
import com.klp.order.infrastructure.event.event.DeliveryArrivedFailedEvent;
import com.klp.order.infrastructure.event.event.DeliveryCreatedEvent;
import com.klp.order.infrastructure.event.event.DeliveryCreatedFailedEvent;
import com.klp.order.infrastructure.event.event.DeliveryShippingEvent;
import com.klp.order.infrastructure.event.event.DeliveryShippingFailedEvent;
import java.util.List;
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
public class DeliveryEventListener {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderRepository orderRepository;


    @KafkaListener(
        topics = "delivery.created",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryCreated(
        @Payload DeliveryCreatedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 배송 생성 이벤트 수신: orderId={}, partition={}, offset={}",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}, 현재 상태: {}",
                order.getOrderId(), order.getOrderStatus());

            if (order.getOrderStatus() == OrderStatus.DELIVERY_CREATED) {
                log.info("이미 처리된 배송 생성 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            // deliveryId 할당
            assignDeliveryIdsToOrderItems(event.products());

            // 주문 상태 변경
            order.changeStatus(OrderStatus.DELIVERY_CREATED);
            orderRepository.save(order);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

            log.info("=== 배송 생성 이벤트 처리 완료: orderId={}, 상태={} ===",
                event.orderId(), OrderStatus.DELIVERY_CREATED);

        } catch (Exception e) {
            log.error("배송 생성 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "delivery.shipping",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryShipping(
        @Payload DeliveryShippingEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 배송 중 이벤트 수신: orderId={}, partition={}, offset={}",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}, 현재 상태: {}",
                order.getOrderId(), order.getOrderStatus());

            if (order.getOrderStatus() == OrderStatus.DELIVERY_SHIPPING) {
                log.info("이미 처리된 배송 중 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }
            // 주문 상태 변경
            order.changeStatus(OrderStatus.DELIVERY_SHIPPING);
            orderRepository.save(order);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

            log.info("=== 배송 중 이벤트 처리 완료: orderId={}, 상태={} ===",
                event.orderId(), OrderStatus.DELIVERY_SHIPPING);

        } catch (Exception e) {
            log.error("배송 중 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "delivery.arrived",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryArrived(
        @Payload DeliveryArrivedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 배송 완료 이벤트 수신: orderId={}, partition={}, offset={}",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}, 현재 상태: {}",
                order.getOrderId(), order.getOrderStatus());

            if (order.getOrderStatus() == OrderStatus.COMPLETE) {
                log.info("이미 처리된 배송 완료 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            // 주문 상태 변경
            order.changeStatus(OrderStatus.COMPLETE);
            orderRepository.save(order);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

            log.info("=== 배송 완료 이벤트 처리 완료: orderId={}, 상태={} ===",
                event.orderId(), OrderStatus.DELIVERY_CREATED);

        } catch (Exception e) {
            log.error("배송 완료 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    /**
     * 배송 생성 실패 이벤트 처리 Spring Kafka가 DeliveryCreatedFailedEvent 타입을 보고 자동으로 이 메서드를 호출
     */
    @KafkaListener(
        topics = "delivery.created.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryCreatedFailed(
        @Payload DeliveryCreatedFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 배송 생성 실패 이벤트 수신: orderId={}, partition={}, offset={}",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}, 현재 상태: {}",
                order.getOrderId(), order.getOrderStatus());

            if (order.getOrderStatus() == OrderStatus.DELIVERY_CREATED_FAILED ||
                order.getOrderStatus() == OrderStatus.FAILED) {
                log.info("이미 처리된 배송 생성 실패 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.DELIVERY_CREATED_FAILED);
            orderRepository.save(order);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

            log.info("=== 배송 생성 실패 이벤트 처리 완료: orderId={}, 상태={} ===",
                event.orderId(), OrderStatus.DELIVERY_CREATED_FAILED);

        } catch (Exception e) {
            log.error("배송 생성 실패 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    /**
     * 배송 중 실패 이벤트 처리 Spring Kafka가 DeliveryShippingFailedEvent 타입을 보고 자동으로 이 메서드를 호출
     */
    @KafkaListener(
        topics = "delivery.shipping.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryShippingFailed(
        @Payload DeliveryShippingFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 배송 중 실패 이벤트 수신: orderId={}, partition={}, offset={}",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}, 현재 상태: {}",
                order.getOrderId(), order.getOrderStatus());

            if (order.getOrderStatus() == OrderStatus.DELIVERY_SHIPPING_FAILED ||
                order.getOrderStatus() == OrderStatus.FAILED) {
                log.info("이미 처리된 배송 중 실패 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.DELIVERY_SHIPPING_FAILED);
            orderRepository.save(order);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

            log.info("=== 배송 중 실패 이벤트 처리 완료: orderId={}, 상태={} ===",
                event.orderId(), OrderStatus.DELIVERY_SHIPPING_FAILED);

        } catch (Exception e) {
            log.error("배송 중 실패 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    /**
     * 배송 완료 실패 이벤트 처리 Spring Kafka가 DeliveryArrivedFailedEvent 타입을 보고 자동으로 이 메서드를 호출
     */
    @KafkaListener(
        topics = "delivery.arrived.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryArrivedFailed(
        @Payload DeliveryArrivedFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 배송 완료 실패 이벤트 수신: orderId={}, partition={}, offset={}",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}, 현재 상태: {}",
                order.getOrderId(), order.getOrderStatus());

            if (order.getOrderStatus() == OrderStatus.DELIVERY_ARRIVED_FAILED ||
                order.getOrderStatus() == OrderStatus.FAILED) {
                log.info("이미 처리된 배송 완료 실패 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.DELIVERY_ARRIVED_FAILED);
            orderRepository.save(order);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

            log.info("=== 배송 완료 실패 이벤트 처리 완료: orderId={}, 상태={} ===",
                event.orderId(), OrderStatus.DELIVERY_ARRIVED_FAILED);

        } catch (Exception e) {
            log.error("배송 완료 실패 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    private void assignDeliveryIdsToOrderItems(List<DeliveryCreatedEvent.OrderItem> deliveryItems) {
        log.info("deliveryId 할당 시작 - 총 {}개 아이템", deliveryItems.size());

        for (DeliveryCreatedEvent.OrderItem item : deliveryItems) {
            try {
                orderItemService.assignDeliveryId(item.orderItemId(), item.deliveryId());
                log.info("deliveryId 할당 완료 - orderItemId: {}, deliveryId: {}",
                    item.orderItemId(), item.deliveryId());
            } catch (Exception e) {
                log.error("deliveryId 할당 실패 - orderItemId: {}, deliveryId: {}",
                    item.orderItemId(), item.deliveryId(), e);
                throw e;
            }
        }

        log.info("모든 deliveryId 할당 완료 - 총 {}개", deliveryItems.size());
    }


    @KafkaListener(
        topics = "delivery.created.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryCreatedDlt(DeliveryCreatedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: DeliveryCreated");
        log.error("⚠️ 배송 생성 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = "delivery.created.failed.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryCreatedFailedDlt(DeliveryCreatedFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: DeliveryCreatedFailed");
        log.error("⚠️ 배송 생성 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = "delivery.shipping.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryShippingDlt(DeliveryShippingEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: DeliveryShipping");
        log.error("⚠️ 배송 중 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = "delivery.shipping.failed.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryShippingFailedDlt(DeliveryShippingFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: DeliveryShippingFailed");
        log.error("⚠️ 배송 중 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = "delivery.arrived.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryArrivedDlt(DeliveryArrivedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: DeliveryArrived");
        log.error("⚠️ 배송 도착 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = "delivery.arrived.failed.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryArrivedFailedDlt(DeliveryArrivedFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: DeliveryArrivedFailed");
        log.error("⚠️ 배송 도착 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

}