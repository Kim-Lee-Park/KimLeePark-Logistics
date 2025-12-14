package com.klp.user.global.config;

import feign.RequestInterceptor;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTracingConfig {

    private final Tracer tracer;

    public FeignTracingConfig(Tracer tracer) {
        this.tracer = tracer;
    }

    @Bean
    public RequestInterceptor traceparentPropagationInterceptor() {
        return template -> {
            Span current = tracer.currentSpan();
            if (current == null) {
                return;
            }

            String traceId = current.context().traceId();
            String spanId = current.context().spanId();

            // W3C traceparent 포맷: 00-<traceId>-<spanId>-01
            String traceparent = String.format("00-%s-%s-01", traceId, spanId);
            template.header("traceparent", traceparent);
        };
    }
}
