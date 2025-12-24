package com.klp.ai.recommendation.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Embedding API", description = "상품 임베딩 관리 API (관리자용)")
public interface EmbeddingControllerDoc {

    @Operation(summary = "단일 상품 임베딩 생성", description = "MASTER 권한으로 특정 상품의 임베딩을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "임베딩 생성 성공"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<String> generateEmbedding(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable UUID productId
    );

    @Operation(summary = "배치 상품 임베딩 생성", description = "MASTER 권한으로 여러 상품의 임베딩을 일괄 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "배치 임베딩 생성 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<String> generateEmbeddings(
        @Parameter(description = "상품 ID 목록", required = true)
        @RequestBody List<UUID> productIds
    );
}
