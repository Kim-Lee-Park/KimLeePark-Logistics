package com.klp.order.order.presentation.dto.order.response.get;

import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.domain.entity.order.OrderStatus;
import com.klp.order.order.presentation.dto.ordercancellation.response.OrderCancellationResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 목록 조회 응답")
public record GetOrdersResponse(
    @Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID orderId,

    @Schema(description = "공급 업체 ID", example = "660e8400-e29b-41d4-a716-446655440000")
    UUID supplierId,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "사용자 쿠폰 ID", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID userCouponId,

    @Schema(description = "주문 상태", example = "PENDING")
    OrderStatus orderStatus,

    @Schema(description = "원가", example = "3000000")
    int originalPrice,

    @Schema(description = "최종 주문 금액", example = "2800000")
    int orderPrice,

    @Schema(description = "취소 정보")
    OrderCancellationResponse cancellation,

    @Schema(description = "생성자 ID", example = "1")
    Long createdBy,

    @Schema(description = "생성일시", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt
) {

    public static GetOrdersResponse from(Order order) {
        return new GetOrdersResponse(
            order.getOrderId(),
            order.getSupplierId(),
            order.getUserId(),
            order.getUserCouponId(),
            order.getOrderStatus(),
            order.getOriginalPrice(),
            order.getOrderPrice(),
            OrderCancellationResponse.from(order.getCancellation()),
            order.getCreatedBy(),
            order.getCreatedAt()
        );
    }
}