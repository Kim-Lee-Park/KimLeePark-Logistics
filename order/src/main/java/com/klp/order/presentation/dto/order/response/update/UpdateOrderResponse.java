package com.klp.order.presentation.dto.order.response.update;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.presentation.dto.orderitem.response.OrderItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "주문 수정 응답")
public record UpdateOrderResponse(
    @Schema(description = "주문 ID", example = "a9f2e7d4-3c8b-4f1a-b6e5-9d2c7f4a8e1b")
    UUID orderId,

    @Schema(description = "공급 업체 ID", example = "5c3d8f1a-9e4b-4d7c-a2f6-1b8e5d9c3a7f")
    UUID supplierId,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "사용자 쿠폰 ID", example = "f1b6d9e2-7a4c-4e8f-9d3b-6c2a5f8e1d4a")
    UUID userCouponId,

    @Schema(description = "주문 코멘트", example = "배송 전 연락 부탁드립니다.")
    String comment,

    @Schema(description = "주문 상태", example = "PENDING")
    OrderStatus orderStatus,

    @Schema(description = "원가", example = "3000000")
    int originalPrice,

    @Schema(description = "쿠폰 할인 금액", example = "150000")
    int couponDiscountPrice,

    @Schema(description = "등급 할인 금액", example = "50000")
    int gradeDiscountPrice,

    @Schema(description = "최종 주문 금액", example = "2800000")
    int orderPrice,

    @Schema(description = "배송지 주소", example = "8e4a2c7f-6d9b-4f3e-a1c5-9f7d2e6b4a8c")
    UUID addressId,

    @Schema(description = "배송지 위도", example = "37.5665")
    BigDecimal deliveryLatitude,

    @Schema(description = "배송지 경도", example = "126.9780")
    BigDecimal deliveryLongitude,

    @Schema(description = "주문 상품 목록")
    List<OrderItemResponse> orderItems,

    @Schema(description = "생성일시", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "생성자 ID", example = "1")
    Long createdBy,

    @Schema(description = "수정일시", example = "2024-01-01T10:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "수정자 ID", example = "1")
    Long updatedBy
) {

    public static UpdateOrderResponse from(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList());

        return new UpdateOrderResponse(
            order.getOrderId(),
            order.getSupplierId(),
            order.getUserId(),
            order.getUserCouponId(),
            order.getComment(),
            order.getOrderStatus(),
            order.getOriginalPrice(),
            order.getCouponDiscountPrice(),
            order.getGradeDiscountPrice(),
            order.getOrderPrice(),
            order.getAddressId(),
            order.getDeliveryLatitude(),
            order.getDeliveryLongitude(),
            orderItemResponses,
            order.getCreatedAt(),
            order.getCreatedBy(),
            order.getUpdatedAt(),
            order.getUpdatedBy()
        );
    }
}