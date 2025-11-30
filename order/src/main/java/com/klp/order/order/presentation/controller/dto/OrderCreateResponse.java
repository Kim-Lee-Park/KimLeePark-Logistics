package com.klp.order.order.presentation.controller.dto;

import java.util.UUID;

public record OrderCreateResponse(
    UUID orderId,
    String version
) {

}
