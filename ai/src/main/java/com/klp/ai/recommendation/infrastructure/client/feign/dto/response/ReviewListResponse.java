package com.klp.ai.recommendation.infrastructure.client.feign.dto.response;

import java.util.List;
import java.util.UUID;

public record ReviewListResponse(
    List<ReviewItem> data
) {

    public record ReviewItem(
        UUID reviewId,
        UUID productId,
        int rating,
        String content
    ) {

    }

    public double getAverageRating() {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        return data.stream()
            .mapToInt(ReviewItem::rating)
            .average()
            .orElse(0.0);
    }

    public int getReviewCount() {
        return data == null ? 0 : data.size();
    }
}
