package com.klp.authservice.auth.infrastructure.jwt;

public interface TokenProvider {

    String generate(Long userId, String userName, String role);
}
