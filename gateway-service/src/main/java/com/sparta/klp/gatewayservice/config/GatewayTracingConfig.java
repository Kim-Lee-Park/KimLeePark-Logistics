package com.sparta.klp.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.http.server.reactive.observation.ServerRequestObservationConvention;

@Configuration
public class GatewayTracingConfig {

    @Bean
    public ServerRequestObservationConvention gatewayHttpServerObservationConvention() {
        return new DefaultServerRequestObservationConvention() {
            @Override
            public String getContextualName(ServerRequestObservationContext context) {
                ServerHttpRequest request = context.getCarrier();
                String method = request.getMethod().toString().toLowerCase();
                String path = request.getURI().getPath();
                return "http " + method + " " + path;
            }
        };
    }
}
