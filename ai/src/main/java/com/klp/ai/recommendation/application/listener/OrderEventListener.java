package com.klp.ai.recommendation.application.listener;

import com.klp.ai.recommendation.application.RecommendationService;
import com.klp.ai.recommendation.domain.event.OrderCancelledEvent;
import com.klp.ai.recommendation.domain.event.OrderCreatedEvent;
import com.klp.ai.recommendation.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = KafkaTopicConfig.ORDER_EVENTS,
    groupId = "recommendation-service-group",
    containerFactory = "recommendationKafkaListenerContainerFactory"
)
public class OrderEventListener {

    private final RecommendationService recommendationService;

    @KafkaHandler
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order 이벤트 수신: orderId={}", event.orderId());

        try {
            recommendationService.generateRecommendations(event.orderId());
        } catch (Exception e) {
            log.error("추천 생성 실패: orderId={}", event.orderId(), e);
        }
    }

    @KafkaHandler
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.debug("주문 취소 이벤트 무시: orderId={}", event.orderId());
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
