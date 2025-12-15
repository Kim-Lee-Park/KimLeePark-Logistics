package com.klp.ai.recommendation.application.service;

import com.klp.ai.recommendation.application.dto.ProductCandidate;
import com.klp.ai.recommendation.application.dto.RecommendationContext;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt() {
        return """
            당신은 물류 플랫폼의 상품 추천 전문가입니다.
            사용자의 주문 이력과 컨텍스트 정보를 바탕으로 적절한 상품을 추천해주세요.
            
            추천 시 고려사항:
            - 재고가 충분한 상품 우선
            - 사용자 허브와 가까운 상품 우선
            - 현재 날씨와 시간대에 적합한 상품
            - 평점이 높은 상품
            
            중요 규칙:
            - 반드시 한국어로만 응답하세요
            - 각 상품은 한 번만 추천하세요 (중복 금지)
            - 응답은 반드시 JSON 배열 형식으로 해주세요
            - 각 추천 항목에는 productId와 reason(추천 이유)을 포함해야 합니다
            """;
    }

    public String buildUserPrompt(RecommendationContext context, List<ProductCandidate> candidates) {
        StringBuilder sb = new StringBuilder();

        sb.append("## 현재 상황\n");
        sb.append(buildContextSection(context));
        sb.append("\n");

        sb.append("## 추천 후보 상품\n");
        sb.append(buildCandidatesSection(candidates));
        sb.append("\n");

        sb.append("## 응답 형식\n");
        sb.append(buildResponseFormatSection());

        return sb.toString();
    }

    private String buildContextSection(RecommendationContext context) {
        StringBuilder sb = new StringBuilder();

        if (context.timeSlot() != null) {
            sb.append(String.format("- 시간대: %s (%s)\n",
                context.timeSlot().getDescription(),
                context.timeSlot().getRecommendedCategories()));
        }

        if (context.weather() != null) {
            sb.append(String.format("- 날씨: %s (기온: %.1f°C, 습도: %d%%)\n",
                context.weather().description(),
                context.weather().temperature(),
                context.weather().humidity()));

            if (context.weather().isRainyOrSnowy()) {
                sb.append("- ⚠️ 비/눈 예보: 우천용품 추천 고려\n");
            }
        } else {
            sb.append("- 날씨: 정보 없음\n");
        }

        if (context.userHub() != null) {
            sb.append(String.format("- 사용자 위치: %s\n", context.userHub().address()));
        }

        return sb.toString();
    }

    private String buildCandidatesSection(List<ProductCandidate> candidates) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < candidates.size(); i++) {
            ProductCandidate c = candidates.get(i);
            sb.append(String.format("%d. %s (index: %d)\n", i + 1, c.productName(), i + 1));
            sb.append(String.format("   - 허브: %s (거리: %.1fkm)\n", c.hubName(), c.distance()));
            sb.append(String.format("   - 재고: %d개%s\n", c.inventory(), c.isLowStock() ? " ⚠️ 재고 부족" : ""));
            sb.append(String.format("   - 평점: %.1f (%d개 리뷰)%s\n",
                c.averageRating(), c.reviewCount(),
                c.isHighlyRated() ? " ⭐ 인기상품" : ""));
        }

        return sb.toString();
    }

    private String buildResponseFormatSection() {
        return """
            아래 JSON 형식으로 상위 5개 상품을 추천해주세요.
            각 상품은 한 번만 추천하고, 중복하지 마세요:
            ```json
            [
              {
                "index": 1,
                "reason": "추천 이유를 자연스러운 한국어로 작성"
              }
            ]
            ```
            
            중요:
            - index는 위 후보 상품 목록의 번호(1부터 시작)를 사용하세요
            - JSON만 출력하고 다른 설명은 하지 마세요
            
            추천 이유 작성 시:
            - 반드시 한국어로만 작성 (영어, 태국어 등 다른 언어 사용 금지)
            - 날씨/시간대와 관련 있으면 해당 내용 언급
            - 관련 없으면 재고, 거리, 평점 등 다른 장점 기반으로 작성
            - 간결하고 친근한 톤으로 1-2문장
            """;
    }
}
