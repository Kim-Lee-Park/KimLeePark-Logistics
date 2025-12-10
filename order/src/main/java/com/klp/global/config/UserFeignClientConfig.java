package com.klp.global.config;

import com.klp.global.exception.UserFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserFeignClientConfig {

    @Bean
    public ErrorDecoder userFeignErrorDecoder() {
        return new UserFeignErrorDecoder();
    }
}
