package com.klp.order.presentation.dto.order.response.delete;

import com.klp.order.domain.entity.order.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 삭제 응답")
public record DeleteOrderResponse(
    @Schema(description = "응답 메시지", example = "주문이 성공적으로 삭제되었습니다.")
    String message,

    @Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID orderId,

    @Schema(description = "삭제자 ID", example = "1")
    Long deletedBy,

    @Schema(description = "삭제일시", example = "2024-01-01T10:00:00")
    LocalDateTime deletedAt
) {

    public static DeleteOrderResponse from(Order order) {
        return new DeleteOrderResponse(
            "주문이 성공적으로 삭제되었습니다.",
            order.getOrderId(),
            order.getDeletedBy(),
            order.getDeletedAt()
        );
    }
}