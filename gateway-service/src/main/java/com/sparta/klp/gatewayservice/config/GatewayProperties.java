package com.sparta.klp.gatewayservice.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 인증 없이 접근 가능한 Public API 경로 목록
 */
@ConfigurationProperties(prefix = "gateway.auth")
public record GatewayProperties(
    List<String> publicPaths
) {

    public GatewayProperties {
        if (publicPaths == null) {
            publicPaths = List.of();
        }
    }
}