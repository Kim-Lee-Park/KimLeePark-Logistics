package com.klp.authservice.auth.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts
                .parser()
                .verifyWith(refreshSecretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUserId(String token) {
        return Jwts
            .parser()
            .verifyWith(refreshSecretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    @Override
    public String getUserName(String token) {
        return Jwts
            .parser()
            .verifyWith(refreshSecretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get(JwtConstants.USERNAME_CLAIM, String.class);
    }

    @Override
    public String getRole(String token) {
        return Jwts
            .parser()
            .verifyWith(refreshSecretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get(JwtConstants.ROLE_CLAIM, String.class);
    }

    @Override
    public LocalDateTime getExpiration(String token) {
        Claims claims = Jwts
            .parser()
            .verifyWith(refreshSecretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getExpiration()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
}
