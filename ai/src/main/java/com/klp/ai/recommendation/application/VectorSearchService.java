package com.klp.ai.recommendation.application;

import com.klp.ai.recommendation.application.dto.ProductRecommendation;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final VectorStore vectorStore;

    @Value("${recommendation.similarity-threshold:0.5}")
    private double similarityThreshold;

    /**
     * 상품명을 기반으로 유사한 상품을 검색합니다.
     *
     * @param productName      검색 기준이 되는 상품명
     * @param excludeProductId 검색 결과에서 제외할 상품 ID (자기 자신)
     * @param topK             반환할 최대 결과 수
     * @return 유사 상품 추천 목록
     */
    public List<ProductRecommendation> findSimilarProducts(String productName, UUID excludeProductId, int topK) {
        List<Document> results = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(productName)
                .topK(topK + 1)
                .similarityThreshold(similarityThreshold)
                .build()
        );

        return results.stream()
            .filter(doc -> {
                String docProductId = (String) doc.getMetadata().get("productId");
                return docProductId != null && !docProductId.equals(excludeProductId.toString());
            })
            .limit(topK)
            .map(doc -> new ProductRecommendation(
                UUID.fromString((String) doc.getMetadata().get("productId")),
                (String) doc.getMetadata().get("productName"),
                doc.getScore() != null ? doc.getScore() : 0.0
            ))
            .toList();
    }
}
