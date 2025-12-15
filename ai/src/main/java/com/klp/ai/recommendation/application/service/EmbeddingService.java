package com.klp.ai.recommendation.application.service;

import com.klp.ai.recommendation.infrastructure.client.feign.HubClient;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.HubResponse;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.ProductResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final HubClient hubClient;
    private final VectorStore vectorStore;

    public void generateAndSaveEmbedding(UUID productId) {
        try {
            ProductResponse product = hubClient.getProduct(productId);
            saveProductEmbedding(product);
            log.info("임베딩 생성 완료: productId={}", productId);
        } catch (Exception e) {
            log.error("임베딩 생성 실패: productId={}", productId, e);
            throw e;
        }
    }

    public void generateAndSaveEmbeddings(List<UUID> productIds) {
        for (UUID productId : productIds) {
            try {
                generateAndSaveEmbedding(productId);
            } catch (Exception e) {
                log.warn("상품 임베딩 생성 실패: productId={}", productId);
            }
        }
    }

    private void saveProductEmbedding(ProductResponse product) {
        String productIdStr = product.productId().toString();

        deleteExistingEmbedding(productIdStr);

        HubResponse hub = hubClient.getHub(product.hubId());
        String embeddingText = createEmbeddingText(product, hub);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", productIdStr);
        metadata.put("productName", product.productName());
        metadata.put("hubId", product.hubId().toString());
        metadata.put("hubName", hub.name());
        metadata.put("latitude", hub.latitude());
        metadata.put("longitude", hub.longitude());

        Document document = new Document(
            productIdStr,
            embeddingText,
            metadata
        );

        vectorStore.add(List.of(document));
    }

    private void deleteExistingEmbedding(String productId) {
        try {
            vectorStore.delete(List.of(productId));
            log.debug("기존 임베딩 삭제: productId={}", productId);
        } catch (Exception e) {
            log.debug("삭제할 기존 임베딩 없음: productId={}", productId);
        }
    }

    private String createEmbeddingText(ProductResponse product, HubResponse hub) {
        return String.format("상품: %s, 허브: %s, 지역: %s",
            product.productName(),
            hub.name(),
            hub.address()
        );
    }
}
