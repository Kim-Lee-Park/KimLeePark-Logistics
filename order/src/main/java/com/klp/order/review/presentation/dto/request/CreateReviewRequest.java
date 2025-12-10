package com.klp.order.review.presentation.dto.request;

import com.klp.order.review.application.command.CreateReviewCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "리뷰 생성 요청")
public record CreateReviewRequest(
    @NotNull(message = "주문 ID는 필수입니다.")
    @Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID orderId,

    @NotNull(message = "상품 ID는 필수입니다.")
    @Schema(description = "상품 ID", example = "660e8400-e29b-41d4-a716-446655440000")
    UUID productId,

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    @Schema(description = "평점 (1-5)", example = "5")
    Integer rating,

    @Size(max = 2000, message = "리뷰 내용은 최대 2000자까지 입력 가능합니다.")
    @Schema(description = "리뷰 내용", example = "정말 좋은 상품입니다!")
    String content
) {

    public CreateReviewCommand toCommand(Long userId) {
        return new CreateReviewCommand(
            orderId,
            productId,
            userId,
            rating,
            content
        );
    }
}
