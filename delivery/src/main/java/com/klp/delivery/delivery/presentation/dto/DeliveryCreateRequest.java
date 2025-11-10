package com.klp.delivery.delivery.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record DeliveryCreateRequest(
    @NotNull(message = "orderId는 필수입니다.")
    UUID orderId,
    @NotNull(message = "supplierId는 필수입니다.")
    Long supplierId,
    @NotNull(message = "customerId는 필수입니다.")
    Long customerId,
    String comment,
    @NotEmpty(message = "orderItems는 필수이며 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemDto> orderItems,
    @NotNull(message = "idempotencyKey는 필수입니다.")
    String idempotencyKey
) {
}

