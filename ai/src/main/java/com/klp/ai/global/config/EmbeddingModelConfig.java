package com.klp.ai.global.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class EmbeddingModelConfig {

    @Bean
    @Primary
    @Profile({"local", "default"})
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
    @Profile("prod")
    public EmbeddingModel openAiEmbeddingModel(
        @Value("${spring.ai.openai.api-key}") String apiKey,
        @Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}") String model
    ) {
        OpenAiApi openAiApi = OpenAiApi.builder().apiKey(apiKey).build();
        return new OpenAiEmbeddingModel(
            openAiApi,
            MetadataMode.EMBED,
            OpenAiEmbeddingOptions.builder().model(model).build()
        );
    }
}
