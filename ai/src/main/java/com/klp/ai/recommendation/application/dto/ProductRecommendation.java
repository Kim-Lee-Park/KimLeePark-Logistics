package com.klp.ai.recommendation.application.dto;

import java.util.UUID;

public record ProductRecommendation(
    UUID productId,
    String productName,
    Double score
) {

}
