package com.sparta.klp.gatewayservice.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtDecoder {

    @Value("${jwt.access.secret}")
    private String accessSecret;

    private SecretKey accessSecretkey;

    @PostConstruct
    public void init() {
        this.accessSecretkey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<Claims> validateAndGetClaims(String token) {
        return Optional.ofNullable(Jwts
            .parser()
            .verifyWith(accessSecretkey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
        );
    }

}
