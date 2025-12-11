package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.event.UserProfileChangedHandler;
import com.klp.order.infrastructure.event.dto.UserProfileChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserProfileChangedHandler userProfileChangedHandler;

    @KafkaListener(
        topics = "user.profile.changed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory")
    public void handleUserProfileChanged(UserProfileChangedMessage msg) {
        log.info("[UserEventListener] user.profile.changed received. userId={}, eventType={}",
            msg.userId(), msg.eventType());
        userProfileChangedHandler.handle(msg);
    }
}
