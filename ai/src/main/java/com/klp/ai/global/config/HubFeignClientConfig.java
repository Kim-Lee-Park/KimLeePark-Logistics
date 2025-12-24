package com.klp.ai.global.config;

import com.klp.ai.global.exception.HubFeignErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HubFeignClientConfig {

    private final FeignHeaderInterceptor feignHeaderInterceptor;

    @Bean
    public ErrorDecoder hubFeignErrorDecoder() {
        return new HubFeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor hubRequestInterceptor() {
        return feignHeaderInterceptor;
    }
}
