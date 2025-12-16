package com.klp.order.infrastructure.client.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jMetricsBindingConfig {

    @Bean
    public MeterBinder resilience4jCircuitBreakerMeterBinder(CircuitBreakerRegistry registry) {
        return meterRegistry ->
            TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(meterRegistry);
    }

    @Bean
    public MeterBinder resilience4jRetryMeterBinder(RetryRegistry registry) {
        return meterRegistry ->
            TaggedRetryMetrics.ofRetryRegistry(registry).bindTo(meterRegistry);
    }
}
