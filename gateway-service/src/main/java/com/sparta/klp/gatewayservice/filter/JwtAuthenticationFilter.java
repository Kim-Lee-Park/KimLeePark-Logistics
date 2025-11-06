package com.sparta.klp.gatewayservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.klp.gatewayservice.config.GatewayProperties;
import com.sparta.klp.gatewayservice.jwt.JwtDecoder;
import com.sparta.klp.gatewayservice.jwt.RequestTokenExtractor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;
    private final GatewayProperties gatewayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        Optional<String> accessToken = RequestTokenExtractor.extractAccessToken(request);

        if (accessToken.isEmpty()) {
            return onError(exchange, "인증 토큰이 필요합니다.");
        }

        String token = accessToken.get();

        try {
            Claims claims = jwtDecoder.validateAndGetClaims(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            ServerHttpRequest upstreamRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Name", URLEncoder.encode(username, StandardCharsets.UTF_8))
                .header("X-User-Role", role)
                .build();

            return chain.filter(exchange.mutate().request(upstreamRequest).build());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            return onError(exchange, "만료된 토큰입니다.");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return onError(exchange, "형식이 올바르지 않은 토큰입니다.");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return onError(exchange, "서명이 유효하지 않은 토큰입니다.");
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage(), e);
            return onError(exchange, "유효하지 않은 토큰입니다.");
        }
    }

    private boolean isPublicPath(String path) {
        return gatewayProperties.publicPaths().stream()
            .anyMatch(path::startsWith);
    }

    /**
     * 비동기 예외 처리 진행(WebFlux 환경) writeWith(Flux.just(buffer))로 비동기 스트림으로 응답 작성
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(
            new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(message);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            log.error("Authentication error: {}", message);
            return response.writeWith(Flux.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            return response.setComplete();
        }
    }
}
