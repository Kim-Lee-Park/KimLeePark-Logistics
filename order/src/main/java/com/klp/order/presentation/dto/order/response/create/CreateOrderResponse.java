package com.klp.order.presentation.dto.order.response.create;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.presentation.dto.ordercancellation.response.OrderCancellationResponse;
import com.klp.order.presentation.dto.orderitem.response.OrderItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "주문 생성 응답")
public record CreateOrderResponse(
    @Schema(description = "주문 ID", example = "UUID")
    UUID orderId,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "공급 업체 ID", example = "UUID")
    UUID supplierId,

    @Schema(description = "사용자 쿠폰 ID", example = "UUID")
    UUID userCouponId,

    @Schema(description = "주문 코멘트", example = "빠른 배송 부탁드립니다.")
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

    @Schema(description = "배송지 주소", example = "내 마음 속")
    String deliveryAddress,

    @Schema(description = "배송지 위도", example = "37.5665")
    BigDecimal deliveryLatitude,

    @Schema(description = "배송지 경도", example = "126.9780")
    BigDecimal deliveryLongitude,

    @Schema(description = "주문 상품 목록")
    List<OrderItemResponse> orderItems,

    @Schema(description = "취소 정보")
    OrderCancellationResponse cancellation,

    @Schema(description = "생성일시", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "생성자 ID", example = "1")
    Long createdBy,

    @Schema(description = "수정일시", example = "2024-01-01T10:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "수정자 ID", example = "1")
    Long updatedBy,

    @Schema(description = "삭제일시", example = "2024-01-01T10:00:00")
    LocalDateTime deletedAt,

    @Schema(description = "삭제자 ID", example = "1")
    Long deletedBy
) {

    public static CreateOrderResponse from(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList());

        return new CreateOrderResponse(
            order.getOrderId(),
            order.getUserId(),
            order.getSupplierId(),
            order.getUserCouponId(),
            order.getComment(),
            order.getOrderStatus(),
            order.getOriginalPrice(),
            order.getCouponDiscountPrice(),
            order.getGradeDiscountPrice(),
            order.getOrderPrice(),
            order.getDeliveryAddress(),
            order.getDeliveryLatitude(),
            order.getDeliveryLongitude(),
            orderItemResponses,
            OrderCancellationResponse.from(order.getCancellation()),
            order.getCreatedAt(),
            order.getCreatedBy(),
            order.getUpdatedAt(),
            order.getUpdatedBy(),
            order.getDeletedAt(),
            order.getDeletedBy()
        );
    }
}