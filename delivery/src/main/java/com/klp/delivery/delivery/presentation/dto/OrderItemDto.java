package com.klp.delivery.delivery.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrderItemDto(
    @NotNull(message = "orderItemId는 필수입니다.")
    UUID orderItemId,
    @NotNull(message = "hubId는 필수입니다.")
    UUID hubId,
    @NotNull(message = "productId는 필수입니다.")
    UUID productId,
    UUID deliveryId,
    @NotNull(message = "quantity는 필수입니다.")
    Integer quantity
) {

}

