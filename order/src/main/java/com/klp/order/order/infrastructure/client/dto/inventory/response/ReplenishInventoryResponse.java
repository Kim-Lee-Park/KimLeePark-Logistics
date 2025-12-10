package com.klp.order.order.infrastructure.client.dto.inventory.response;

import java.util.List;
import java.util.UUID;

public record ReplenishInventoryResponse(
    List<UUID> productIds
) {

}
