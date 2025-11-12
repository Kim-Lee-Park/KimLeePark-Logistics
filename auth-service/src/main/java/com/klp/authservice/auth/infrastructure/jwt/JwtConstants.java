package com.klp.authservice.auth.infrastructure.jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtConstants {

    public static final String HEADER_TYPE = "JWT";
    public static final String ISSUER = "KLP-BE";
    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    public static final String USERNAME_CLAIM = "username";
    public static final String ROLE_CLAIM = "role";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final int TOKEN_PREFIX_LENGTH = 7;
}
