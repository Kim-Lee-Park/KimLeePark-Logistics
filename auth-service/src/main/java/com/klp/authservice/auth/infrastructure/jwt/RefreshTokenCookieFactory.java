package com.klp.authservice.auth.infrastructure.jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RefreshTokenCookieFactory {

    public static ResponseCookie create(String refreshToken) {
        return ResponseCookie
            .from(JwtConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(60 * 60 * 24 * 7L)
            .sameSite("Lax")
            .build();
    }

    public static ResponseCookie invalidate() {
        return ResponseCookie
            .from(JwtConstants.REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();
    }
}
