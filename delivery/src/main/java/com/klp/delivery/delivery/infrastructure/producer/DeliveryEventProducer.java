package com.klp.delivery.delivery.infrastructure.producer;

import com.klp.delivery.delivery.domain.event.DeliveryNotificationEvent;
import com.klp.delivery.delivery.domain.event.OrderDeliveryEvent;
import com.klp.delivery.global.config.KafkaTopicConfig;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliveryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 배송 생성 이벤트 발행
     */
    public void publishCreatedEvent(OrderDeliveryEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_EVENTS, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("배송 생성 이벤트 발행 성공: orderId={}, status={}", event.orderId(), event.status());
            } else {
                log.error("배송 생성 이벤트 발행 실패: orderId={}, status={}, error={}", event.orderId(), event.status(),
                    ex.getMessage());
            }
        });
    }

    /**
     * 배송 중 이벤트 발행
     */
    public void publishShippingEvent(OrderDeliveryEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_EVENTS, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("배송 중 이벤트 발행 성공: orderId={}, status={}", event.orderId(), event.status());
            } else {
                log.error("배송 중 이벤트 발행 실패: orderId={}, status={}, error={}", event.orderId(), event.status(),
                    ex.getMessage());
            }
        });
    }

    /**
     * 배송 완료 이벤트 발행
     */
    public void publishArrivedEvent(OrderDeliveryEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_EVENTS, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("배송 완료 이벤트 발행 성공: orderId={}, status={}", event.orderId(), event.status());
            } else {
                log.error("배송 완료 이벤트 발행 실패: orderId={}, status={}, error={}", event.orderId(), event.status(),
                    ex.getMessage());
            }
        });
    }

    /**
     * 배송 알림 이벤트 발행
     */
    public void publishNotificationEvent(DeliveryNotificationEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.DELIVERY_EVENTS, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("배송 알림 이벤트 발행 성공: orderId={}, departureHubName={}", event.orderId(), event.departureHubName());
            } else {
                log.error("배송 알림 이벤트 발행 실패: orderId={}, error={}", event.orderId(), ex.getMessage());
            }
        });
    }
}

