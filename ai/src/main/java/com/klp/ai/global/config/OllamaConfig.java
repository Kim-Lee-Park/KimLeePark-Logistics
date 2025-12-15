package com.klp.ai.global.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "default"})
public class OllamaConfig {

    @Bean
    @Primary
    public EmbeddingModel ollamaEmbeddingModel(
        @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
        @Value("${spring.ai.ollama.embedding.options.model:bge-m3}") String model
    ) {
        OllamaApi ollamaApi = OllamaApi.builder().baseUrl(baseUrl).build();
        return OllamaEmbeddingModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(OllamaEmbeddingOptions.builder().model(model).build())
            .build();
    }

    @Bean
    @Primary
    public ChatModel ollamaChatModel(
        @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
        @Value("${spring.ai.ollama.chat.options.model:llama3.2}") String model
    ) {
        OllamaApi ollamaApi = OllamaApi.builder().baseUrl(baseUrl).build();
        return OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(OllamaChatOptions.builder().model(model).build())
            .build();
    }
}
