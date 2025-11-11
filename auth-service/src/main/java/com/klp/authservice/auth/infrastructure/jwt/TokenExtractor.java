package com.klp.authservice.auth.infrastructure.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenExtractor {
    
    public static Optional<String> extractRefreshToken(HttpServletRequest request) {
        Map<String, String> cookies = Optional.ofNullable(request.getCookies())
            .map(cookieArray -> Arrays.stream(cookieArray)
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue)))
            .orElse(Collections.emptyMap());

        return JwtParser.extractRefreshToken(cookies);
    }
}
