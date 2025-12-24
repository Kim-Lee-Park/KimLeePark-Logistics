package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.enums.DeliveryRouteStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusUpdateRequest(
    @NotNull(message = "status는 필수입니다.")
    DeliveryRouteStatus status
) {

}

