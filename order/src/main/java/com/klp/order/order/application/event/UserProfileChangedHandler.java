package com.klp.order.order.application.event;

import com.klp.order.order.application.cache.UserProfileCache;
import com.klp.order.order.infrastructure.event.dto.UserProfileChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileChangedHandler {

    private final UserProfileCache userProfileCache;

    public void handle(UserProfileChangedMessage msg) {
        switch (msg.eventType()) {
            case "PROFILE_UPDATED" -> userProfileCache.evictUserProfile(msg.userId());
            default ->
                log.warn("[UserProfileChangedHandler] Unknown eventType received: {} for userId={}",
                    msg.eventType(), msg.userId());
        }
    }
}
