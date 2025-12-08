package com.klp.ai.recommendation.presentation.docs;

import com.klp.ai.recommendation.presentation.dto.response.RecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Recommendation API", description = "AI 기반 상품 추천 API")
public interface RecommendationControllerDoc {

    @Operation(summary = "상품 추천 조회", description = "주문 ID를 기반으로 AI가 추천하는 상품 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "추천 조회 성공"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<RecommendationResponse> getRecommendations(
        @Parameter(description = "주문 ID", required = true)
        @PathVariable UUID orderId,
        @Parameter(description = "추천 상품 개수 (기본값: 10)")
        @RequestParam(defaultValue = "10") int limit
    );
}
