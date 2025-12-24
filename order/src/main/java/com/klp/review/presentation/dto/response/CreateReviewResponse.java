package com.klp.review.presentation.dto.response;

import com.klp.review.domain.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "리뷰 생성 응답")
public record CreateReviewResponse(
    @Schema(description = "리뷰 ID", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID reviewId,

    @Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID orderId,

    @Schema(description = "상품 ID", example = "660e8400-e29b-41d4-a716-446655440000")
    UUID productId,

    @Schema(description = "작성자 ID", example = "123")
    Long userId,

    @Schema(description = "평점 (1-5)", example = "5")
    int rating,

    @Schema(description = "리뷰 내용", example = "정말 좋은 상품입니다!")
    String content,

    @Schema(description = "생성 일시", example = "2024-12-09T15:30:00")
    LocalDateTime createdAt,

    @Schema(description = "생성자 ID", example = "123")
    Long createdBy,

    @Schema(description = "수정 일시", example = "2024-12-09T16:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "수정자 ID", example = "123")
    Long updatedBy
) {

    public static CreateReviewResponse from(Review review) {
        return new CreateReviewResponse(
            review.getReviewId(),
            review.getOrderId(),
            review.getProductId(),
            review.getUserId(),
            review.getRating(),
            review.getContent(),
            review.getCreatedAt(),
            review.getCreatedBy(),
            review.getUpdatedAt(),
            review.getUpdatedBy()
        );
    }
}
