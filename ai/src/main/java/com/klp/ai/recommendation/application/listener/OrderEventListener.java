package com.klp.ai.recommendation.application.listener;

import com.klp.ai.recommendation.application.RecommendationFacade;
import com.klp.ai.recommendation.application.dto.OrderedProduct;
import com.klp.ai.recommendation.application.dto.RecommendationResult;
import com.klp.ai.recommendation.domain.event.OrderCancelledEvent;
import com.klp.ai.recommendation.domain.event.OrderCreatedEvent;
import com.klp.ai.recommendation.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.List;
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

    private final RecommendationFacade recommendationFacade;

    @KafkaHandler
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order 이벤트 수신: orderId={}", event.orderId());

        try {
            List<OrderedProduct> orderedProducts = event.products().stream()
                .map(item -> new OrderedProduct(
                    item.productId(),
                    item.productName()
                ))
                .toList();

            List<RecommendationResult> recommendations = recommendationFacade.generateRecommendations(
                event.userId(),
                event.userAddressHubId(),
                orderedProducts
            );

            log.info("RAG 추천 생성 완료: orderId={}, count={}", event.orderId(), recommendations.size());
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
