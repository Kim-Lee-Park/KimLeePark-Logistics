package com.sparta.klp.gatewayservice.jwt;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtParser {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_PREFIX_LENGTH = 7;

    public static Optional<String> extractTokenFromHeader(String authorizationHeader) {
        log.info("test deploy gateway v4");
        return Optional.ofNullable(authorizationHeader)
            .filter(header -> header.startsWith(TOKEN_PREFIX))
            .map(header -> header.substring(TOKEN_PREFIX_LENGTH))
            .filter(token -> !token.isEmpty());
    }
}
