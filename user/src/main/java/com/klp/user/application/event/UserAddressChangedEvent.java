package com.klp.user.application.event;

import java.util.UUID;

public record UserAddressChangedEvent(
    UUID addressId
) {

}
