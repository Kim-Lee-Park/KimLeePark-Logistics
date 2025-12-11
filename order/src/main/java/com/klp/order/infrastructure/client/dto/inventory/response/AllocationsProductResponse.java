package com.klp.order.infrastructure.client.dto.inventory.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AllocationsProductResponse(
    UUID reservationId,
    UUID productId,
    int allocatedQuantity,
    int remainingStock,
    String status,
    LocalDateTime expiresAt
) {

}
