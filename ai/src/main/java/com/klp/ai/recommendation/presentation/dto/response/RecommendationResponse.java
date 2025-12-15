package com.klp.ai.recommendation.presentation.dto.response;

import com.klp.ai.recommendation.application.dto.RecommendationResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
    UUID orderId,
    List<RecommendationItem> recommendations,
    LocalDateTime generatedAt
) {

    public record RecommendationItem(
        UUID productId,
        String productName,
        String hubName,
        double distance,
        int inventory,
        double averageRating,
        int reviewCount,
        double score,
        String reason
    ) {

        public static RecommendationItem from(RecommendationResult r) {
            return new RecommendationItem(
                r.productId(), r.productName(),
                r.hubName(), r.distance(), r.inventory(),
                r.averageRating(), r.reviewCount(), r.score(), r.reason()
            );
        }
    }

    public static RecommendationResponse of(UUID orderId, List<RecommendationResult> results) {
        List<RecommendationItem> items = results.stream()
            .map(RecommendationItem::from)
            .toList();
        return new RecommendationResponse(orderId, items, LocalDateTime.now());
    }
}
