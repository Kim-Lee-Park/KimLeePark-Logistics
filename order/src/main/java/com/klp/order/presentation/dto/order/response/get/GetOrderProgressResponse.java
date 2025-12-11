package com.klp.order.presentation.dto.order.response.get;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 진행 상태 응답")
public record GetOrderProgressResponse(
    @Schema(description = "진행 중인 주문 여부", example = "true")
    boolean isOrderProgressing
) {

    public static GetOrderProgressResponse of(boolean isOrderProgressing) {
        return new GetOrderProgressResponse(isOrderProgressing);
    }
}