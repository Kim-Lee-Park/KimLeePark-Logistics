package com.klp.delivery.delivery.infrastructure.producer;

import com.klp.delivery.delivery.application.event.DeliveryEventPublisher;
import com.klp.delivery.delivery.domain.event.DeliveryNotificationEvent;
import com.klp.delivery.delivery.domain.event.OrderDeliveryEvent;
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
    public void publishCreatedEvent(OrderDeliveryEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_EVENTS, key, event);
            log.info("배송 생성 이벤트 발행 완료: orderId={}, status={}", event.orderId(), event.status());
        } catch (Exception e) {
            log.error("배송 생성 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    @Override
    public void publishShippingEvent(OrderDeliveryEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_EVENTS, key, event);
            log.info("배송 중 이벤트 발행 완료: orderId={}, status={}", event.orderId(), event.status());
        } catch (Exception e) {
            log.error("배송 중 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    @Override
    public void publishArrivedEvent(OrderDeliveryEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_EVENTS, key, event);
            log.info("배송 완료 이벤트 발행 완료: orderId={}, status={}", event.orderId(), event.status());
        } catch (Exception e) {
            log.error("배송 완료 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    @Override
    public void publishNotificationEvent(DeliveryNotificationEvent event) {
        try {
            String key = event.orderId().toString();
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_EVENTS, key, event);
            log.info("배송 알림 이벤트 발행 완료: orderId={}, departureHubName={}", 
                event.orderId(), event.departureHubName());
        } catch (Exception e) {
            log.error("배송 알림 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }
}

