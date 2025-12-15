package com.klp.notification.messaging.application.listener;

import com.klp.notification.ai.application.AIService;
import com.klp.notification.ai.application.command.GenerateMessageCommand;
import com.klp.notification.messaging.domain.event.DeliveryNotificationEvent;
import com.klp.notification.messaging.infrastructure.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = KafkaTopicConfig.DELIVERY_CREATED_TOPIC,
    groupId = "notification-service-group",
    containerFactory = "notificationKafkaListenerContainerFactory"
)
public class DeliveryEventListener {

    private final AIService aiService;

    @KafkaHandler
    public void handleDeliveryCreated(DeliveryNotificationEvent event) {
        log.info("배송 생성 이벤트 수신: deliveryId={}, orderId={}", event.deliveryId(), event.orderId());

        try {
            GenerateMessageCommand command = new GenerateMessageCommand(
                event.driverSlackId(),
                event.orderId(),
                event.ordererName(),
                event.ordererEmail(),
                event.orderTime(),
                event.productName(),
                event.quantity(),
                event.requirements(),
                event.departureHubName(),
                event.transitHubNames(),
                event.destinationAddress(),
                event.driverName(),
                event.driverEmail(),
                event.workingHours()
            );

            aiService.fromTextInput(command);
            log.info("AI 메시지 생성 요청 완료: driverSlackId={}", event.driverSlackId());
        } catch (Exception e) {
            log.error("배송 생성 알림 처리 실패: deliveryId={}, error={}", event.deliveryId(), e.getMessage());
            throw e;
        }
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
