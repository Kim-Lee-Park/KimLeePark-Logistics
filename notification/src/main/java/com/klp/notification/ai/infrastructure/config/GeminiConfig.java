package com.klp.notification.ai.infrastructure.config;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Bean
    public Client geminiClient() {
        return Client.builder()
            .apiKey(apiKey)
            .build();
    }

    @Bean
    public GenerateContentConfig deliveryDeadlineConfig() {
        Content systemInstruction = Content.fromParts(
            Part.fromText("""
                당신은 물류 배송 전문가입니다. 물건을 보내는 발송 담당자가 납기일시를 맞추기 위한 최종 발송 시한을 계산하세요.
                
                [고려사항]
                - 배송 담당자 근무시간: 09:00 - 18:00
                - 각 허브 간 평균 이동 시간: 4-6시간
                - 허브 상하차 및 처리 시간: 2-3시간
                - 교통 상황 여유 시간: 10-20%
                """)
        );

        return GenerateContentConfig.builder()
            .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0).build())
            .candidateCount(1)
            .maxOutputTokens(100)
            .systemInstruction(systemInstruction)
            .temperature(0.2F)
            .build();
    }
}
