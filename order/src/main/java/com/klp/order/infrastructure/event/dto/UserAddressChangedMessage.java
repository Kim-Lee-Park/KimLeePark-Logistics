package com.klp.order.infrastructure.event.dto;

import java.util.UUID;

public record UserAddressChangedMessage(
    UUID addressId,
    String eventType
) {

}
