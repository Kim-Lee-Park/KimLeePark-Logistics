package com.klp.ai.global.config;

import com.klp.ai.global.exception.OrderFeignErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OrderFeignClientConfig {

    private final FeignHeaderInterceptor feignHeaderInterceptor;

    @Bean
    public ErrorDecoder orderFeignErrorDecoder() {
        return new OrderFeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor orderRequestInterceptor() {
        return feignHeaderInterceptor;
    }
}
