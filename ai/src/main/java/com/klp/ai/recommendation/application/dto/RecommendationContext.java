package com.klp.ai.recommendation.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecommendationContext(
    Long userId,
    HubInfo userHub,
    List<OrderedProduct> orderedProducts,
    LocalDateTime requestTime,
    WeatherInfo weather,
    TimeSlot timeSlot
) {

}
