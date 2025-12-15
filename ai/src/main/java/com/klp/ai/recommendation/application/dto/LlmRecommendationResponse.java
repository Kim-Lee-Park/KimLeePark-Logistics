package com.klp.ai.recommendation.application.dto;

public record LlmRecommendationResponse(
    Integer index,
    String reason
) {

    public boolean hasValidIndex() {
        return index != null && index > 0;
    }
}
