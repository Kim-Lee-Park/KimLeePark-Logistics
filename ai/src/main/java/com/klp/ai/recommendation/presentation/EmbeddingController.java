package com.klp.ai.recommendation.presentation;

import com.klp.ai.recommendation.application.service.EmbeddingService;
import com.klp.ai.recommendation.presentation.docs.EmbeddingControllerDoc;
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
public class EmbeddingController implements EmbeddingControllerDoc {

    private final EmbeddingService embeddingService;

    @PostMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB')")
    public ResponseEntity<String> generateEmbedding(@PathVariable UUID productId) {
        embeddingService.generateAndSaveEmbedding(productId);
        return ResponseEntity.ok("임베딩 생성 완료: " + productId);
    }

    @PostMapping("/products/batch")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB')")
    public ResponseEntity<String> generateEmbeddings(@RequestBody List<UUID> productIds) {
        embeddingService.generateAndSaveEmbeddings(productIds);
        return ResponseEntity.ok("배치 임베딩 생성 완료: " + productIds.size() + "개");
    }
}
