package com.klp.order.presentation.dto.order.response.cancel;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.presentation.dto.ordercancellation.response.OrderCancellationResponse;
import com.klp.order.presentation.dto.orderitem.response.OrderItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "주문 취소 응답")
public record CancelOrderResponse(
    @Schema(description = "주문 ID", example = "UUID")
    UUID orderId,

    @Schema(description = "주문 상태 (CANCELLED)", example = "CANCELLED")
    OrderStatus orderStatus,

    @Schema(description = "공급 업체 ID", example = "UUID")
    UUID supplierId,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "사용자 쿠폰 ID", example = "UUID")
    UUID userCouponId,

    @Schema(description = "원가", example = "3000000")
    int originalPrice,

    @Schema(description = "쿠폰 할인 금액", example = "150000")
    int couponDiscountPrice,

    @Schema(description = "등급 할인 금액", example = "50000")
    int gradeDiscountPrice,

    @Schema(description = "최종 주문 금액", example = "2800000")
    int orderPrice,

    @Schema(description = "주문 상품 목록")
    List<OrderItemResponse> orderItems,

    @Schema(description = "취소 정보 (취소 사유, 취소자 등)")
    OrderCancellationResponse cancellation,

    @Schema(description = "생성일시", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "생성자 ID", example = "1")
    Long createdBy,

    @Schema(description = "수정일시", example = "2024-01-01T10:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "수정자 ID", example = "1")
    Long updatedBy
) {

    public static CancelOrderResponse from(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList());

        return new CancelOrderResponse(
            order.getOrderId(),
            order.getOrderStatus(),
            order.getSupplierId(),
            order.getUserId(),
            order.getUserCouponId(),
            order.getOriginalPrice(),
            order.getCouponDiscountPrice(),
            order.getGradeDiscountPrice(),
            order.getOrderPrice(),
            orderItemResponses,
            OrderCancellationResponse.from(order.getCancellation()),
            order.getCreatedAt(),
            order.getCreatedBy(),
            order.getUpdatedAt(),
            order.getUpdatedBy()
        );
    }
}