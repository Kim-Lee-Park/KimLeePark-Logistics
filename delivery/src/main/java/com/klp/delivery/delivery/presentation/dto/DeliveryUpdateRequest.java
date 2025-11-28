package com.klp.delivery.delivery.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record DeliveryUpdateRequest(
    @NotNull(message = "변경 할 배송담당자 ID는 필수 입니다.")
    Long vendorDrvierId
) {

}

