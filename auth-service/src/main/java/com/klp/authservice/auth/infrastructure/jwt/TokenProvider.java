package com.klp.authservice.auth.infrastructure.jwt;

import java.time.LocalDateTime;

public interface TokenProvider {

    String generate(Long userId, String username, String role);

    boolean validateToken(String token);

    String getUserId(String token);

    String getUserName(String token);

    String getRole(String token);

    LocalDateTime getExpiration(String token);
}
