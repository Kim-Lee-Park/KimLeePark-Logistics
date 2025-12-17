package com.klp.user.infrastructure.event.dto;

import java.util.UUID;

public record UserAddressChangedMessage(
    UUID addressId,
    String eventType
) {

}
