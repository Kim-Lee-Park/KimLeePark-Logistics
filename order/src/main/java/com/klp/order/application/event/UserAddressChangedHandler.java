package com.klp.order.application.event;

import com.klp.order.application.cache.UserAddressCache;
import com.klp.order.infrastructure.event.dto.UserAddressChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressChangedHandler {

    private final UserAddressCache userAddressCache;

    public void handle(UserAddressChangedMessage msg) {
        switch (msg.eventType()) {
            case "ADDRESS_UPDATED" -> userAddressCache.evictUserAddress(msg.addressId());
            default ->
                log.warn("[UserAddressChangedHandler] Unknown eventType received: {} for userId={}",
                    msg.eventType(), msg.addressId());
        }
    }
}
