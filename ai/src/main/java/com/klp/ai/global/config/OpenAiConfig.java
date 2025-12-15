package com.klp.ai.global.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"prod"})
public class OpenAiConfig {

    @Bean
    @Primary
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

    @Bean
    @Primary
    public ChatModel ollamaChatModel(
        @Value("${spring.ai.openai.api-key}") String apiKey,
        @Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}") String model
    ) {
        OpenAiApi openAiApi = OpenAiApi.builder().apiKey(apiKey).build();
        return OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(OpenAiChatOptions.builder().model(model).build())
            .build();
    }
}
