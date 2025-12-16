package com.klp.hub.global.config.resilience;

import com.klp.hub.common.exception.ExternalApiErrorCode;
import com.klp.hub.common.exception.ExternalApiException;
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
                    return be.getErrorCode() == ExternalApiErrorCode.DELIVERY_SERVICE_UNAVAILABLE
                        || be.getErrorCode() == ExternalApiErrorCode.ORDER_SERVICE_UNAVAILABLE;
                }
                return false;
            })
            .build();

        RetryRegistry registry = RetryRegistry.of(retryConfig);

        registry.retry("deliveryService", retryConfig);
        registry.retry("orderService", retryConfig);

        return registry;
    }
}
