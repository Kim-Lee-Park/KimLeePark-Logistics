package com.klp.ai.recommendation.application;

import com.klp.ai.recommendation.infrastructure.client.feign.HubClient;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.ProductResponse;
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

    /**
     * 단일 상품의 임베딩을 생성하고 벡터 스토어에 저장합니다.
     */
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

    /**
     * 여러 상품의 임베딩을 배치로 생성합니다.
     */
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

        // 기존 임베딩 삭제 (중복 방지)
        deleteExistingEmbedding(productIdStr);

        String embeddingText = createEmbeddingText(product);

        Document document = new Document(
            productIdStr,
            embeddingText,
            Map.of(
                "productId", productIdStr,
                "productName", product.productName(),
                "companyName", product.companyName(),
                "hubId", product.hubId().toString()
            )
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

    private String createEmbeddingText(ProductResponse product) {
        return String.format("상품: %s, 회사: %s",
            product.productName(),
            product.companyName()
        );
    }
}
