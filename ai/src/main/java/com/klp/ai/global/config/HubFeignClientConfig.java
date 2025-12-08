package com.klp.ai.global.config;

import com.klp.ai.global.exception.HubFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HubFeignClientConfig {

    @Bean
    public ErrorDecoder hubFeignErrorDecoder() {
        return new HubFeignErrorDecoder();
    }
}
