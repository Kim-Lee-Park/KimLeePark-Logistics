package com.klp.delivery.global.config.resilience;

import com.klp.delivery.common.exception.ExternalApiErrorCode;
import com.klp.delivery.common.exception.ExternalApiException;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.UnknownHostException;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceRetryRegistryConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(2)
            .waitDuration(Duration.ofMillis(300))
            .retryOnException(ex -> {
                if (ex instanceof feign.RetryableException) {
                    return true;
                }
                if (ex instanceof UnknownHostException) {
                    return true;
                }
                if (ex instanceof ExternalApiException be) {
                    return be.getErrorCode() == ExternalApiErrorCode.HUB_SERVICE_UNAVAILABLE
                        || be.getErrorCode() == ExternalApiErrorCode.DRIVER_SERVICE_UNAVAILABLE;
                }
                return false;
            })
            .build();

        RetryRegistry registry = RetryRegistry.of(retryConfig);

        registry.retry("hubService", retryConfig);
        registry.retry("userService", retryConfig);

        return registry;
    }
}
