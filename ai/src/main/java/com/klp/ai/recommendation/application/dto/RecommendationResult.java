package com.klp.ai.recommendation.application.dto;

import java.util.UUID;

public record RecommendationResult(
    UUID productId,
    String productName,
    String companyName,
    UUID hubId,
    String hubName,
    double distance,
    int inventory,
    double averageRating,
    int reviewCount,
    double score,
    String reason
) {

    public static RecommendationResult from(ProductCandidate candidate, double score, String reason) {
        return new RecommendationResult(
            candidate.productId(),
            candidate.productName(),
            candidate.companyName(),
            candidate.hubId(),
            candidate.hubName(),
            candidate.distance(),
            candidate.inventory(),
            candidate.averageRating(),
            candidate.reviewCount(),
            score,
            reason
        );
    }
}
