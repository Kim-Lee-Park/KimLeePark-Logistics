package com.klp.authservice.auth.infrastructure.jwt;

import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtParser {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";

    public static Optional<String> extractAccessToken(String authorizationHeader) {
        return Optional.ofNullable(authorizationHeader)
            .filter(header -> header.startsWith(JwtConstants.TOKEN_PREFIX))
            .map(header -> header.substring(JwtConstants.TOKEN_PREFIX_LENGTH))
            .filter(token -> !token.isEmpty());
    }

    public static Optional<String> extractRefreshToken(Map<String, String> cookies) {
        return Optional.ofNullable(cookies)
            .map(map -> map.get(REFRESH_TOKEN_COOKIE_NAME))
            .filter(token -> !token.isEmpty());
    }
}
