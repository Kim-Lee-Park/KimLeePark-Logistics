package com.klp.delivery.delivery.infrastructure.producer;

import com.klp.delivery.delivery.application.event.DeliveryEventPublisher;
import com.klp.delivery.delivery.domain.event.DeliveryArrivedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryArrivedFailedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedFailedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryShippingEvent;
import com.klp.delivery.delivery.domain.event.DeliveryShippingFailedEvent;
import com.klp.delivery.global.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryProducer implements DeliveryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishCreatedEvent(DeliveryCreatedEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_CREATED_TOPIC, key, event);
            log.info("배송 생성 이벤트 발행 완료: orderId={}, status={}", event.orderId());
        } catch (Exception e) {
            log.error("배송 생성 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    @Override
    public void publishShippingEvent(DeliveryShippingEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_SHIPPING_TOPIC, key, event);
            log.info("배송 중 이벤트 발행 완료: orderId={}, status={}", event.orderId());
        } catch (Exception e) {
            log.error("배송 중 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    @Override
    public void publishArrivedEvent(DeliveryArrivedEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_ARRIVED_TOPIC, key, event);
            log.info("배송 완료 이벤트 발행 완료: orderId={}, status={}", event.orderId());
        } catch (Exception e) {
            log.error("배송 완료 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    @Override
    public void publishCreatedFailedEvent(DeliveryCreatedFailedEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_CREATED_FAILED_TOPIC, key, event);
            log.info("배송 생성 실패 이벤트 발행 완료: orderId={}, departureHubName={}",
                event.orderId());
        } catch (Exception e) {
            log.error("배송 생성 실패  이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    @Override
    public void publishShippingFailedEvent(DeliveryShippingFailedEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_SHIPPING_FAILED_TOPIC, key, event);
            log.info("배송 중 실패 이벤트 발행 완료: orderId={}, departureHubName={}",
                event.orderId());
        } catch (Exception e) {
            log.error("배송 중 실패  이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    @Override
    public void publishArrivedFailedEvent(DeliveryArrivedFailedEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_ARRIVED_FAILED_TOPIC, key, event);
            log.info("배송 완료 실패 이벤트 발행 완료: orderId={}, departureHubName={}",
                event.orderId());
        } catch (Exception e) {
            log.error("배송 완료 실패  이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }
}

