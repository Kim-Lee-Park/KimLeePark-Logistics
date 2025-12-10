package com.klp.review.presentation.dto.request;

import com.klp.review.application.command.UpdateReviewCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "리뷰 수정 요청")
public record UpdateReviewRequest(
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    @Schema(description = "평점 (1-5)", example = "4")
    Integer rating,

    @Size(max = 2000, message = "리뷰 내용은 최대 2000자까지 입력 가능합니다.")
    @Schema(description = "리뷰 내용", example = "수정된 리뷰 내용입니다.")
    String content
) {
    public UpdateReviewCommand toCommand() {
        return new UpdateReviewCommand(rating, content);
    }
}
