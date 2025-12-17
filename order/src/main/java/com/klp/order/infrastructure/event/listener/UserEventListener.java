package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.event.UserAddressChangedHandler;
import com.klp.order.application.event.UserProfileChangedHandler;
import com.klp.order.infrastructure.event.dto.UserAddressChangedMessage;
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
    private final UserAddressChangedHandler userAddressChangedHandler;

    @KafkaListener(
        topics = "user.profile.changed",
        groupId = "order-service-group",
        containerFactory = "userProfileChangedKafkaListenerContainerFactory")
    public void handleUserProfileChanged(UserProfileChangedMessage msg) {
        log.info("[UserEventListener] user.profile.changed received. userId={}, eventType={}",
            msg.userId(), msg.eventType());
        userProfileChangedHandler.handle(msg);
    }

    @KafkaListener(
        topics = "user.address.changed",
        groupId = "order-service-group",
        containerFactory = "userAddressChangedKafkaListenerContainerFactory")
    public void handleUserProfileChanged(UserAddressChangedMessage msg) {
        log.info("[UserEventListener] user.address.changed received. userId={}, eventType={}",
            msg.addressId(), msg.eventType());
        userAddressChangedHandler.handle(msg);
    }
}
