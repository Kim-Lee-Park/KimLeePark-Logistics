package com.klp.ai.recommendation.presentation;

import com.klp.ai.recommendation.application.EmbeddingService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/embeddings")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    /**
     * 단일 상품 임베딩 생성 (관리자용)
     */
    @PostMapping("/products/{productId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<String> generateEmbedding(@PathVariable UUID productId) {
        embeddingService.generateAndSaveEmbedding(productId);
        return ResponseEntity.ok("임베딩 생성 완료: " + productId);
    }

    /**
     * 여러 상품 임베딩 배치 생성 (관리자용)
     */
    @PostMapping("/products/batch")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<String> generateEmbeddings(@RequestBody List<UUID> productIds) {
        embeddingService.generateAndSaveEmbeddings(productIds);
        return ResponseEntity.ok("배치 임베딩 생성 완료: " + productIds.size() + "개");
    }
}
