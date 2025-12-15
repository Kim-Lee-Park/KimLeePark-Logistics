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
import java.util.Set;
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

            Set<Integer> seenIndices = new HashSet<>();
            List<RecommendationResult> results = new ArrayList<>();

            for (LlmRecommendationResponse r : llmResponses) {
                if (!r.hasValidIndex()) {
                    continue;
                }

                int idx = r.index() - 1;
                if (idx < 0 || idx >= candidates.size()) {
                    continue;
                }

                if (seenIndices.contains(r.index())) {
                    continue;
                }

                seenIndices.add(r.index());
                ProductCandidate candidate = candidates.get(idx);
                String cleanedReason = cleanReason(r.reason());
                results.add(RecommendationResult.from(candidate, candidate.similarityScore(), cleanedReason));
            }

            return results;
        } catch (Exception e) {
            log.error("LLM 응답 파싱 실패", e);
            throw new RuntimeException("LLM 응답 파싱 실패", e);
        }
    }

    private String extractJsonContent(String response) {
        String content = response;

        if (content.contains("```json")) {
            int start = content.indexOf("```json") + 7;
            int end = content.indexOf("```", start);
            if (end > start) {
                content = content.substring(start, end).trim();
            }
        } else if (content.contains("```")) {
            int start = content.indexOf("```") + 3;
            int end = content.indexOf("```", start);
            if (end > start) {
                content = content.substring(start, end).trim();
            }
        }

        // JSON 주석 제거 (// 스타일)
        content = content.replaceAll("//.*", "");
        // JSON 주석 제거 (/* */ 스타일)
        content = content.replaceAll("/\\*.*?\\*/", "");
        // 빈 줄 정리
        content = content.replaceAll("(?m)^\\s*$[\r\n]*", "");

        return content.trim();
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
