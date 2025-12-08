package com.klp.ai.recommendation.presentation;

import com.klp.ai.recommendation.application.RecommendationService;
import com.klp.ai.recommendation.application.dto.ProductRecommendation;
import com.klp.ai.recommendation.presentation.docs.RecommendationControllerDoc;
import com.klp.ai.recommendation.presentation.dto.response.RecommendationResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationControllerDoc {

    private final RecommendationService recommendationService;

    @GetMapping("/{orderId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<RecommendationResponse> getRecommendations(
        @PathVariable UUID orderId,
        @RequestParam(defaultValue = "10") int limit) {

        List<ProductRecommendation> recommendations = recommendationService.getRecommendations(orderId);

        return ResponseEntity.ok().body(new RecommendationResponse(
            orderId,
            recommendations.stream().limit(limit).toList(),
            LocalDateTime.now()
        ));
    }
}
