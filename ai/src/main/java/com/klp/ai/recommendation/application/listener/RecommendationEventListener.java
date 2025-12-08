package com.klp.ai.recommendation.application.listener;

import com.klp.ai.recommendation.application.RecommendationService;
import com.klp.ai.recommendation.domain.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationEventListener {

    private final RecommendationService recommendationService;

    @KafkaListener(topics = "payment.completed", groupId = "ai-group")
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        dltTopicSuffix = ".dlt"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신: orderId={}", event.orderId());

        // 동기 호출 (예외 전파되어야 재시도 동작)
        recommendationService.generateRecommendations(event.orderId());
    }

    @DltHandler
    public void handleDlt(PaymentCompletedEvent event) {
        log.error("추천 생성 최종 실패 (DLT): orderId={}", event.orderId());
        // TODO: 알림 발송 또는 수동 처리 대기열에 저장
    }
}
