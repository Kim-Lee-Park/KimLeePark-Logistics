package com.klp.authservice.global.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRetryerConfig {

    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }
}
