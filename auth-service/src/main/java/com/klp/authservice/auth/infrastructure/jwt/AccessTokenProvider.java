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
public class AccessTokenProvider implements TokenProvider {

    @Value("${jwt.access.secret}")
    private String accessSecret;
    @Value("${jwt.access.expiration}")
    private Long accessExpiration;

    private SecretKey accessSecretKey;

    @PostConstruct
    public void init() {
        this.accessSecretKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
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
            .claim(JwtConstants.TOKEN_TYPE_CLAIM, JwtConstants.ACCESS_TOKEN_TYPE)
            .claim(JwtConstants.USERNAME_CLAIM, userName)
            .claim(JwtConstants.ROLE_CLAIM, role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessExpiration))
            .signWith(accessSecretKey)
            .compact();
    }
}
