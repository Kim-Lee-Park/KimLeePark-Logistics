package com.klp.authservice.auth.infrastructure.external.config.resilience;

import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.authservice.global.exception.BusinessException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.net.UnknownHostException;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceCircuitBreakerRegistryConfig {

    //실패 누적(카운트) 대상 선별
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .failureRateThreshold(50)
            .permittedNumberOfCallsInHalfOpenState(2)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .recordException(ex -> {
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

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(cbConfig);

        registry.circuitBreaker("userService", cbConfig);

        return registry;
    }
}
