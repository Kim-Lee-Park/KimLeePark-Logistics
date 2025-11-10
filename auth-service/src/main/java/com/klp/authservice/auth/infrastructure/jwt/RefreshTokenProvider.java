package com.klp.authservice.auth.infrastructure.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenProvider implements TokenProvider {

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;
    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    private SecretKey refreshSecretKey;

    @PostConstruct
    public void init() {
        this.refreshSecretKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generate(Long userId, String userName, String role) {
        return Jwts
            .builder()
            .header()
            .type(JwtConstants.HEADER_TYPE)
            .and()
            .issuer(JwtConstants.ISSUER)
            .subject(userId.toString())
            .claim(JwtConstants.TOKEN_TYPE_CLAIM, JwtConstants.REFRESH_TOKEN_TYPE)
            .claim(JwtConstants.USERNAME_CLAIM, userName)
            .claim(JwtConstants.ROLE_CLAIM, role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(refreshSecretKey)
            .compact();
    }
}
