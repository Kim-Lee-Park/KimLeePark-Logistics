package com.klp.order.order.application.service.dto;

import java.util.UUID;

public record OrderResponse(
    UUID orderId
) {
}
