package com.klp.hub.product.application.event;

import java.util.UUID;

public record ProductInfoChangedEvent(
    UUID productId
) {

}
