package com.klp.ai.recommendation.application;

import com.klp.ai.recommendation.application.dto.HubInfo;
import com.klp.ai.recommendation.application.dto.OrderedProduct;
import com.klp.ai.recommendation.application.dto.ProductCandidate;
import com.klp.ai.recommendation.application.dto.RecommendationContext;
import com.klp.ai.recommendation.application.dto.RecommendationResult;
import com.klp.ai.recommendation.application.service.ContextService;
import com.klp.ai.recommendation.application.service.LlmService;
import com.klp.ai.recommendation.application.service.OrderDataService;
import com.klp.ai.recommendation.application.service.VectorSearchService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationFacade {

    private final VectorSearchService vectorSearchService;
    private final ContextService contextService;
    private final LlmService llmService;
    private final OrderDataService orderDataService;

    @Value("${recommendation.top-k:10}")
    private int topK;

    /**
     * 주문 ID 기반 추천 생성
     */
    public List<RecommendationResult> getRecommendations(
        UUID orderId,
        Long userId,
        UUID userHubId,
        int limit
    ) {
        log.info("추천 요청: orderId={}, userId={}", orderId, userId);
        
        List<OrderedProduct> orderedProducts = orderDataService.getOrderedProducts(orderId);

        List<RecommendationResult> recommendations = generateRecommendations(
            userId, userHubId, orderedProducts
        );

        return recommendations.stream().limit(limit).toList();
    }

    /**
     * RAG 파이프라인으로 추천 생성
     */
    public List<RecommendationResult> generateRecommendations(
        Long userId,
        UUID userHubId,
        List<OrderedProduct> orderedProducts
    ) {
        if (orderedProducts.isEmpty()) {
            log.warn("주문 상품이 없어 추천 불가: userId={}", userId);
            return Collections.emptyList();
        }

        List<ProductCandidate> availableCandidates = Collections.emptyList();

        try {
            List<ProductCandidate> candidates = searchCandidates(orderedProducts);
            
            if (candidates.isEmpty()) {
                log.warn("유사 상품 없음: userId={}", userId);
                return Collections.emptyList();
            }

            RecommendationContext context = contextService.buildContext(
                userId, userHubId, orderedProducts
            );

            List<ProductCandidate> enrichedCandidates = enrichCandidates(candidates, context.userHub());

            availableCandidates = filterByInventory(enrichedCandidates);
            
            if (availableCandidates.isEmpty()) {
                log.warn("재고 있는 상품 없음: userId={}", userId);
                return Collections.emptyList();
            }

            List<RecommendationResult> results = llmService.generateRecommendations(context, availableCandidates);
            log.info("추천 완료: userId={}, 결과={} 개", userId, results.size());
            return results;

        } catch (Exception e) {
            log.error("추천 생성 실패: userId={}", userId, e);
            return fallbackToVectorResults(availableCandidates);
        }
    }

    private List<ProductCandidate> searchCandidates(List<OrderedProduct> orderedProducts) {
        String searchQuery = orderedProducts.stream()
            .map(OrderedProduct::productName)
            .collect(Collectors.joining(" "));

        UUID excludeProductId = orderedProducts.get(0).productId();

        return vectorSearchService.findSimilarProductsWithContext(
            searchQuery, excludeProductId, topK
        );
    }

    private List<ProductCandidate> enrichCandidates(
        List<ProductCandidate> candidates,
        HubInfo userHub
    ) {
        return candidates.stream()
            .map(candidate -> contextService.enrichCandidate(
                candidate.productId(),
                candidate.productName(),
                candidate.hubId(),
                candidate.hubName(),
                candidate.similarityScore(),
                userHub
            ))
            .toList();
    }

    private List<ProductCandidate> filterByInventory(List<ProductCandidate> candidates) {
        return candidates.stream()
            .filter(c -> c.inventory() > 0)
            .toList();
    }

    private List<RecommendationResult> fallbackToVectorResults(List<ProductCandidate> candidates) {
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        return candidates.stream()
            .limit(5)
            .map(c -> RecommendationResult.from(
                c,
                c.similarityScore(),
                "고객님께 추천하는 상품입니다."
            ))
            .toList();
    }
}
