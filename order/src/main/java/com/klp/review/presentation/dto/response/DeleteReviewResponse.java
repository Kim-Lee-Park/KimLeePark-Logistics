package com.klp.review.presentation.dto.response;

import com.klp.review.domain.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "리뷰 삭제 응답")
public record DeleteReviewResponse(
    @Schema(description = "리뷰 ID", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID reviewId,

    @Schema(description = "삭제 일시", example = "2024-12-09T17:00:00")
    LocalDateTime deletedAt,

    @Schema(description = "삭제자 ID", example = "123")
    Long deletedBy,

    @Schema(description = "메시지", example = "리뷰가 삭제되었습니다.")
    String message
) {

    public static DeleteReviewResponse from(Review review) {
        return new DeleteReviewResponse(
            review.getReviewId(),
            review.getDeletedAt(),
            review.getDeletedBy(),
            "리뷰가 삭제되었습니다."
        );
    }
}
