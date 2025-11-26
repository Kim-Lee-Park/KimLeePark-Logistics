package com.klp.authservice.auth.infrastructure.external.config;

import com.klp.authservice.auth.infrastructure.external.UserFeignErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserFeignClientConfig {

    @Bean
    public UserFeignErrorDecoder errorDecoder() {
        return new UserFeignErrorDecoder();
    }
}
