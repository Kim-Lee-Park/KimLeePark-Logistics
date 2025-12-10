package com.klp.order.order.presentation.dto.orderitem.response;

import com.klp.order.order.domain.entity.orderitem.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 상품 응답")
public record OrderItemResponse(
    @Schema(description = "주문 상품 ID", example = "990e8400-e29b-41d4-a716-446655440000")
    UUID orderItemId,

    @Schema(description = "상품 ID", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID productId,

    @Schema(description = "상품명", example = "노트북")
    String productName,

    @Schema(description = "허브 ID", example = "880e8400-e29b-41d4-a716-446655440000")
    UUID hubId,

    @Schema(description = "주문 수량", example = "2")
    Integer quantity,

    @Schema(description = "상품 단가", example = "1500000")
    Integer unitPrice,

    @Schema(description = "총 가격", example = "3000000")
    Integer totalPrice,

    @Schema(description = "배송 ID", example = "aa0e8400-e29b-41d4-a716-446655440000")
    UUID deliveryId,

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

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
            orderItem.getOrderItemId(),
            orderItem.getProductId(),
            orderItem.getProductName(),
            orderItem.getHubId(),
            orderItem.getQuantity(),
            orderItem.getPrice(),
            orderItem.getTotalPrice(),
            orderItem.getDeliveryId(),
            orderItem.getCreatedAt(),
            orderItem.getCreatedBy(),
            orderItem.getUpdatedAt(),
            orderItem.getUpdatedBy(),
            orderItem.getDeletedAt(),
            orderItem.getDeletedBy()
        );
    }
}