package com.klp.ai.recommendation.application.service;

import com.klp.ai.recommendation.application.dto.ProductCandidate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final VectorStore vectorStore;

    @Value("${recommendation.similarity-threshold:0.5}")
    private double similarityThreshold;

    /**
     * 상품명을 기반으로 유사한 상품을 검색합니다.
     */
    public List<ProductCandidate> findSimilarProductsWithContext(
        String productName,
        UUID excludeProductId,
        int topK
    ) {
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
            .map(this::toProductCandidate)
            .toList();
    }

    private ProductCandidate toProductCandidate(Document doc) {
        Map<String, Object> metadata = doc.getMetadata();

        return new ProductCandidate(
            UUID.fromString((String) metadata.get("productId")),
            (String) metadata.get("productName"),
            UUID.fromString((String) metadata.get("hubId")),
            (String) metadata.get("hubName"),
            0.0,
            0,
            doc.getScore() != null ? doc.getScore() : 0.0,
            0.0,
            0
        );
    }
}
