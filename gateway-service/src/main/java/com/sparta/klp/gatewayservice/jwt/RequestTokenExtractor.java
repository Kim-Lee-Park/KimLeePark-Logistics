package com.sparta.klp.gatewayservice.jwt;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestTokenExtractor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static Optional<String> extractAccessToken(ServerHttpRequest request) {
        String authorizationHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        return JwtParser.extractTokenFromHeader(authorizationHeader);
    }

}
