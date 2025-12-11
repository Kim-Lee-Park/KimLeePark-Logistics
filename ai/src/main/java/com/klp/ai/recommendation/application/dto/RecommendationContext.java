package com.klp.ai.recommendation.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RecommendationContext(
    UUID userId,
    HubInfo userHub,
    List<OrderedProduct> orderedProducts,
    LocalDateTime requestTime,
    WeatherInfo weather,
    TimeSlot timeSlot
) {

}
