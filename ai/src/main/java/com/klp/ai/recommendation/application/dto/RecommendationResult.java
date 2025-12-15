package com.klp.ai.recommendation.application.dto;

import java.util.UUID;

public record RecommendationResult(
    UUID productId,
    String productName,
    UUID hubId,
    String hubName,
    double distance,
    int inventory,
    double averageRating,
    int reviewCount,
    double similarityScore
) {

    public static RecommendationResult from(ProductCandidate candidate) {
        return new RecommendationResult(
            candidate.productId(),
            candidate.productName(),
            candidate.hubId(),
            candidate.hubName(),
            candidate.distance(),
            candidate.inventory(),
            candidate.averageRating(),
            candidate.reviewCount(),
            candidate.similarityScore()
        );
    }
}
