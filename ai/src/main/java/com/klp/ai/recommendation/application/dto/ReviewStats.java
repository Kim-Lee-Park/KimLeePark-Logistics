package com.klp.ai.recommendation.application.dto;

import java.util.UUID;

public record ReviewStats(
    UUID productId,
    double averageRating,
    int reviewCount
) {

    public boolean isHighlyRated() {
        return averageRating >= 4.0 && reviewCount >= 5;
    }

    public boolean hasEnoughReviews() {
        return reviewCount >= 5;
    }

    public static ReviewStats empty(UUID productId) {
        return new ReviewStats(productId, 0.0, 0);
    }
}
