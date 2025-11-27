package com.klp.order.presentation.dto.order.response.get;

public record GetOrderProgressResponse(
    boolean isOrderProgressing
) {

    public static GetOrderProgressResponse of(boolean isOrderProgressing) {
        return new GetOrderProgressResponse(isOrderProgressing);
    }
}
