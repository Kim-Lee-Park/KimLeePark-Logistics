package com.klp.ai.recommendation.infrastructure.client.feign.dto.response;

import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID orderId,
    List<OrderItemDto> orderItems
) {

    public record OrderItemDto(
        UUID productId,
        String productName,
        UUID hubId
    ) {

    }
}
