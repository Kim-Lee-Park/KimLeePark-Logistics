package com.klp.authservice.auth.infrastructure.external.config;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * 임시로 RestClient로 작성. 추후 FeignClient로 변경 예정. Timeout 설정도 임시로 비활성화
 */
@Configuration
public class RestClientConfig {

    @Value("${clients.user.base-url}")
    private String userClientUrl;

    @Bean
    public RestClient userRestClient() {
        return RestClient.builder()
            .baseUrl(userClientUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
