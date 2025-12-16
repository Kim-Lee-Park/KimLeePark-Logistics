package com.klp.authservice.auth.infrastructure.external.config.resilience;

import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.authservice.global.exception.BusinessException;
import feign.RetryableException;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.UnknownHostException;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceRetryRegistryConfig {

    //재시도 대상 선별
    @Bean
    public RetryRegistry retryRegistry() {

        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(2)
            .waitDuration(Duration.ofMillis(300))
            .retryOnException(ex -> {
                if (ex instanceof RetryableException) {
                    return true;
                }
                if (ex instanceof UnknownHostException) {
                    return true;
                }

                if (ex instanceof BusinessException be) {
                    return be.getErrorCode() == AuthErrorCode.USER_SERVICE_UNAVAILABLE
                        || be.getErrorCode() == AuthErrorCode.USER_SERVICE_ERROR;
                }
                return false;
            })
            .build();

        RetryRegistry registry = RetryRegistry.of(retryConfig);

        registry.retry("userService", retryConfig);

        return registry;
    }
}
