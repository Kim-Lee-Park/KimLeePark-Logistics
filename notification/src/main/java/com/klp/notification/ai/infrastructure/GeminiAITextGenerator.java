package com.klp.notification.ai.infrastructure;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.klp.notification.ai.domain.AITextGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAITextGenerator implements AITextGenerator {

    private final Client client;

    @Override
    public String generate(String prompt) {
        log.info("Gemini AI 텍스트 생성 시작");
        
        GenerateContentResponse response = client.models.generateContent(
            "gemini-2.5-flash",
            prompt,
            null
        );

        log.info("Gemini AI 텍스트 생성 완료");
        return response.text();
    }
}
