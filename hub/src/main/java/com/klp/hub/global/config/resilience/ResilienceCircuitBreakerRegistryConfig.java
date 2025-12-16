package com.klp.hub.global.config.resilience;

import com.klp.hub.common.exception.ExternalApiErrorCode;
import com.klp.hub.common.exception.ExternalApiException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.net.UnknownHostException;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceCircuitBreakerRegistryConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .failureRateThreshold(50)
            .permittedNumberOfCallsInHalfOpenState(2)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .recordException(ex -> {
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

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(cbConfig);

        registry.circuitBreaker("deliveryService", cbConfig);
        registry.circuitBreaker("orderService", cbConfig);

        return registry;
    }
}
