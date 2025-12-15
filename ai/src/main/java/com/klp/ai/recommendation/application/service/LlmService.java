package com.klp.ai.recommendation.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.ai.recommendation.application.dto.LlmRecommendationResponse;
import com.klp.ai.recommendation.application.dto.ProductCandidate;
import com.klp.ai.recommendation.application.dto.RecommendationContext;
import com.klp.ai.recommendation.application.dto.RecommendationResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final ChatClient.Builder chatClientBuilder;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public List<RecommendationResult> generateRecommendations(
        RecommendationContext context,
        List<ProductCandidate> candidates
    ) {
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String prompt = promptBuilder.buildPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(context, candidates);

            ChatClient chatClient = chatClientBuilder.build();

            String response = chatClient.prompt()
                .system(prompt)
                .user(userPrompt)
                .call()
                .content();

            return parseResponse(response, candidates);
        } catch (Exception e) {
            log.error("LLM 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("LLM 추천 생성 실패", e);
        }
    }

    private List<RecommendationResult> parseResponse(
        String response,
        List<ProductCandidate> candidates
    ) {
        try {
            String jsonContent = extractJsonContent(response);

            List<LlmRecommendationResponse> llmResponses = objectMapper.readValue(
                jsonContent,
                new TypeReference<List<LlmRecommendationResponse>>() {
                }
            );

            Map<UUID, ProductCandidate> candidateMap = candidates.stream()
                .collect(Collectors.toMap(ProductCandidate::productId, Function.identity()));

            // 중복 제거: productId 기준으로 첫 번째 항목만 유지
            Set<UUID> seenProductIds = new HashSet<>();
            List<RecommendationResult> results = new ArrayList<>();

            for (LlmRecommendationResponse r : llmResponses) {
                UUID productId = r.getProductIdAsUUID();
                if (productId != null && candidateMap.containsKey(productId) && !seenProductIds.contains(productId)) {
                    seenProductIds.add(productId);
                    ProductCandidate candidate = candidateMap.get(productId);
                    String cleanedReason = cleanReason(r.reason());
                    results.add(RecommendationResult.from(candidate, candidate.similarityScore(), cleanedReason));
                }
            }

            return results;
        } catch (Exception e) {
            log.error("LLM 응답 파싱 실패: response={}", response, e);
            throw new RuntimeException("LLM 응답 파싱 실패", e);
        }
    }

    private String extractJsonContent(String response) {
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        return response.trim();
    }

    /**
     * LLM 응답에서 비한국어/비영어 문자 제거
     */
    private String cleanReason(String reason) {
        if (reason == null) {
            return "고객님께 추천드리는 상품입니다.";
        }
        // 한글, 영문, 숫자, 기본 문장부호만 허용
        String cleaned = reason.replaceAll("[^가-힣a-zA-Z0-9\\s.,!?%°~\\-()]", "").trim();
        return cleaned.isEmpty() ? "고객님께 추천드리는 상품입니다." : cleaned;
    }
}
