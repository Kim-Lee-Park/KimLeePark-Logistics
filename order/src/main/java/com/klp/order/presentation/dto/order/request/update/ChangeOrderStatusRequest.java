package com.klp.order.presentation.dto.order.request.update;

import com.klp.order.domain.entity.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeOrderStatusRequest(
    @NotNull(message = "주문 상태는 필수입니다.")
    OrderStatus orderStatus
) {

}
