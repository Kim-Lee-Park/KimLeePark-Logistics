package com.klp.ai.recommendation.presentation.dto.response;

import com.klp.ai.recommendation.application.dto.ProductRecommendation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
    UUID orderId,
    List<ProductRecommendation> recommendations,
    LocalDateTime generatedAt
) {

}