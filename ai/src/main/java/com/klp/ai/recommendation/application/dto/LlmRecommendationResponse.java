package com.klp.ai.recommendation.application.dto;

import java.util.UUID;

public record LlmRecommendationResponse(
    String productId,
    String reason
) {

    public UUID getProductIdAsUUID() {
        return UUID.fromString(productId);
    }
}
