package com.klp.order.presentation.dto.order.request.update;

import com.klp.order.domain.entity.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상태 변경 요청")
public record ChangeOrderStatusRequest(

    @Schema(
        description = "변경할 주문 상태",
        example = "PAID",
        required = true,
        allowableValues = {
            "PENDING", "CREATED", "PAID", "STOCK_CONFIRMED",
            "COUPON_CONFIRMED", "DELIVERY_CREATED", "DELIVERY_SHIPPING",
            "COMPLETE", "CANCELLED", "FAILED"
        }
    )
    @NotNull(message = "주문 상태는 필수입니다.")
    OrderStatus orderStatus
) {

}
