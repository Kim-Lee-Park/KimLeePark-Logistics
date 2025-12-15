package com.klp.ai.recommendation.application.dto;

import java.util.UUID;

public record ProductCandidate(
    UUID productId,
    String productName,
    UUID hubId,
    String hubName,
    double distance,
    int inventory,
    double similarityScore,
    double averageRating,
    int reviewCount
) {

    public boolean isLowStock() {
        return inventory > 0 && inventory < 5;
    }

    public boolean isHighlyRated() {
        return averageRating >= 4.0 && reviewCount >= 5;
    }
}
