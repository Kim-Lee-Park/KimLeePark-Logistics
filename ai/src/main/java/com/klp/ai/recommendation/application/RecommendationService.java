package com.klp.ai.recommendation.application;

import com.klp.ai.recommendation.application.dto.ProductRecommendation;
import com.klp.ai.recommendation.infrastructure.client.OrderClient;
import com.klp.ai.recommendation.infrastructure.client.dto.response.OrderResponse;
import com.klp.ai.recommendation.infrastructure.client.dto.response.OrderResponse.OrderItemDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final OrderClient orderClient;
    private final VectorSearchService vectorSearchService;
    private final RecommendationCacheService cacheService;

    /**
     * 추천 상품 조회 (캐시 히트 시 캐시 반환, 미스 시 실시간 생성)
     */
    public List<ProductRecommendation> getRecommendations(UUID orderId) {
        Optional<List<ProductRecommendation>> cached = cacheService.getRecommendations(orderId);
        if (cached.isPresent()) {
            log.debug("캐시 히트: orderId={}", orderId);
            return cached.get();
        }

        log.debug("캐시 미스: orderId={}", orderId);
        return generateRecommendations(orderId);
    }

    /**
     * 추천 생성
     */
    public List<ProductRecommendation> generateRecommendations(UUID orderId) {
        OrderResponse order = orderClient.getOrder(orderId);

        List<ProductRecommendation> recommendations = new ArrayList<>();

        for (OrderItemDto item : order.orderItems()) {
            List<ProductRecommendation> similarProducts = vectorSearchService.findSimilarProducts(
                item.productName(),
                item.productId(),
                5
            );
            recommendations.addAll(similarProducts);
        }

        List<ProductRecommendation> topRecommendations = recommendations.stream()
            .collect(Collectors.toMap(
                ProductRecommendation::productId,
                r -> r,
                (r1, r2) -> r1.score() > r2.score() ? r1 : r2
            ))
            .values().stream()
            .sorted(Comparator.comparing(ProductRecommendation::score).reversed())
            .limit(5)
            .toList();

        cacheService.saveRecommendations(orderId, topRecommendations);

        log.info("추천 생성 완료: orderId={}, count={}", orderId, topRecommendations.size());
        return topRecommendations;
    }
}
