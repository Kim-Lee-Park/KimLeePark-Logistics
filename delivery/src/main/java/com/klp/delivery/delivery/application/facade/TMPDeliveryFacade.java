package com.klp.delivery.delivery.application.facade;

import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.event.DeliveryEventPublisher;
import com.klp.delivery.delivery.domain.event.DeliveryArrivedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryArrivedFailedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedFailedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryShippingEvent;
import com.klp.delivery.delivery.domain.event.DeliveryShippingFailedEvent;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TMPDeliveryFacade {

    private final DeliveryEventPublisher publisher;

    @Transactional
    public void createDelivery(OrderToDeliveryCommand orderCommand,
        IdempotencyCommand idempotencyCommand) {

        log.info("=== [배송 임시] 배송 생성 시작: orderId={} ===", orderCommand.orderId());

        try {
            // 1. deliveryId를 포함한 OrderItem 리스트 생성
            List<DeliveryCreatedEvent.OrderItem> orderItems = new ArrayList<>();
            for (OrderItemCommand item : orderCommand.products()) {
                UUID tempDeliveryId = UUID.randomUUID();
//                orderItems.add(new DeliveryCreatedEvent.OrderItem(
//                    item.orderItemId(),
//                    tempDeliveryId
//                ));

                log.debug("임시 배송 매핑: orderItemId={}, deliveryId={}",
                    item.orderItemId(), tempDeliveryId);
            }

            // 2. 이벤트 생성 및 발행
            DeliveryCreatedEvent event = DeliveryCreatedEvent.from(orderCommand, orderItems);
            publisher.publishCreatedEvent(event);

            log.info("=== [배송 임시] 배송 생성 완료 이벤트 발행: orderId={}, itemCount={} ===",
                orderCommand.orderId(), orderItems.size());

            publisher.publishShippingEvent(new DeliveryShippingEvent(event.orderId()));
            log.info("=== [배송 임시] 배송 중 이벤트 발행: orderId={}",
                orderCommand.orderId());

            publisher.publishArrivedEvent(new DeliveryArrivedEvent(event.orderId()));
            log.info("=== [배송 임시] 배송 완료 이벤트 발행: orderId={}",
                orderCommand.orderId());

        } catch (Exception e) {
            log.error("=== [배송 임시] 이벤트 처리 실패: orderId={} ===",
                orderCommand.orderId(), e);
            throw e;
        }
    }

    @Transactional
    public void startShipping(UUID orderId) {
        log.info("=== [배송 임시] 배송 출발: orderId={} ===", orderId);

        try {
            publisher.publishShippingEvent(new DeliveryShippingEvent(orderId));
            log.info("=== [배송 임시] 배송 출발 이벤트 발행 완료: orderId={} ===", orderId);
        } catch (Exception e) {
            log.error("=== [배송 임시] 배송 출발 이벤트 발행 실패: orderId={} ===", orderId, e);
            throw e;
        }
    }

    @Transactional
    public void completeDelivery(UUID orderId) {
        log.info("=== [배송 임시] 배송 완료: orderId={} ===", orderId);

        try {
            publisher.publishArrivedEvent(new DeliveryArrivedEvent(orderId));
            log.info("=== [배송 임시] 배송 완료 이벤트 발행 완료: orderId={} ===", orderId);
        } catch (Exception e) {
            log.error("=== [배송 임시] 배송 완료 이벤트 발행 실패: orderId={} ===", orderId, e);
            throw e;
        }
    }

    @Transactional
    public void failDeliveryCreation(OrderToDeliveryCommand orderCommand, Exception cause) {
        log.warn("=== [배송 임시] 배송 생성 실패: orderId={}, cause={} ===",
            orderCommand.orderId(), cause.getMessage());

        try {
            DeliveryCreatedFailedEvent event = DeliveryCreatedFailedEvent.from(orderCommand);
            publisher.publishCreatedFailedEvent(event);

            log.info("=== [배송 임시] 배송 생성 실패 이벤트 발행 완료: orderId={} ===",
                orderCommand.orderId());
        } catch (Exception e) {
            log.error("=== [배송 임시] 배송 생성 실패 이벤트 발행 실패: orderId={} ===",
                orderCommand.orderId(), e);
        }
    }

    @Transactional
    public void failShipping(UUID orderId) {
        log.warn("=== [배송 임시] 배송 출발 실패: orderId={} ===", orderId);

        try {
            publisher.publishShippingFailedEvent(new DeliveryShippingFailedEvent(orderId));
            log.info("=== [배송 임시] 배송 출발 실패 이벤트 발행 완료: orderId={} ===", orderId);
        } catch (Exception e) {
            log.error("=== [배송 임시] 배송 출발 실패 이벤트 발행 실패: orderId={} ===", orderId, e);
        }
    }

    @Transactional
    public void failArrival(UUID orderId) {
        log.warn("=== [배송 임시] 배송 완료 실패: orderId={} ===", orderId);

        try {
            publisher.publishArrivedFailedEvent(new DeliveryArrivedFailedEvent(orderId));
            log.info("=== [배송 임시] 배송 완료 실패 이벤트 발행 완료: orderId={} ===", orderId);
        } catch (Exception e) {
            log.error("=== [배송 임시] 배송 완료 실패 이벤트 발행 실패: orderId={} ===", orderId, e);
        }
    }
}