package com.sparta.klp.gatewayservice.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;

@Configuration
public class GatewayObservationExcludeConfig {

    @Bean
    public ObservationPredicate gatewayExcludeHealthAndMetrics() {
        return (name, context) -> {

            /* -------------------------
             * 1) Gateway로 들어오는 HTTP 요청 필터링
             * ------------------------- */
            if (context instanceof ServerRequestObservationContext serverCtx) {
                ServerHttpRequest request = (ServerHttpRequest) serverCtx.getCarrier();

                String path = request.getURI().getPath();
                String method = request.getMethod().toString();

                if ("GET".equals(method)
                    && (path.contains("/actuator/prometheus"))) {
                    return false;
                }
            }
            return true;
        };
    }
}
