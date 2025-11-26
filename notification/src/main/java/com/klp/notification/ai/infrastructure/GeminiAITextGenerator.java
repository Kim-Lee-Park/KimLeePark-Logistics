package com.klp.notification.ai.infrastructure;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.klp.notification.ai.domain.AITextGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Google Gemini를 사용한 AI 텍스트 생성기 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAITextGenerator implements AITextGenerator {

    private final Client client;

    @Override
    public String generate(String prompt) {
        log.debug("Generating text with Gemini AI for prompt: {}", prompt);

        GenerateContentResponse response = client.models.generateContent(
            "gemini-2.5-flash",
            prompt,
            null
        );

        String generatedText = response.text();
        log.debug("Generated text: {}", generatedText);

        return generatedText;
    }
}
