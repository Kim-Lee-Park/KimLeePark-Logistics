package com.sparta.klp.gatewayservice.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtDecoder 단위 테스트")
class JwtDecoderTest {

    private JwtDecoder jwtDecoder;
    private SecretKey secretKey;
    private static final String TEST_SECRET = "testSecretKeytestSecretKeytestSecretKey";

    @BeforeEach
    void setUp() {
        jwtDecoder = new JwtDecoder();
        secretKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        ReflectionTestUtils.setField(jwtDecoder, "accessSecret", TEST_SECRET);
        jwtDecoder.init();
    }

    @Test
    @DisplayName("유효한 JWT 토큰을 검증하고 Claims를 반환한다")
    void shouldValidateValidTokenAndReturnClaims() {
        // given
        String userId = "1";
        String username = "test";
        String role = "MASTER";

        String token = Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(secretKey)
            .compact();

        // when
        Optional<Claims> result = jwtDecoder.validateAndGetClaims(token);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSubject()).isEqualTo(userId);
        assertThat(result.get().get("username", String.class)).isEqualTo(username);
        assertThat(result.get().get("role", String.class)).isEqualTo(role);
    }

    @Test
    @DisplayName("만료된 토큰은 ExpiredJwtException을 발생시킨다")
    void shouldThrowExceptionForExpiredToken() {
        // given
        String token = Jwts.builder()
            .subject("user123")
            .issuedAt(new Date(System.currentTimeMillis() - 7200000))
            .expiration(new Date(System.currentTimeMillis() - 3600000))
            .signWith(secretKey)
            .compact();

        // when & then
        assertThat(org.assertj.core.api.Assertions.catchThrowable(
            () -> jwtDecoder.validateAndGetClaims(token)
        )).isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    @DisplayName("잘못된 서명의 토큰은 SignatureException을 발생시킨다")
    void shouldThrowExceptionForInvalidSignature() {
        // given
        SecretKey wrongKey = Keys.hmacShaKeyFor(
            "wrongSecretKeywrongSecretKeywrongSecretKey".getBytes(StandardCharsets.UTF_8)
        );

        String token = Jwts.builder()
            .subject("user123")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(wrongKey)
            .compact();

        // when & then
        assertThat(org.assertj.core.api.Assertions.catchThrowable(
            () -> jwtDecoder.validateAndGetClaims(token)
        )).isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 예외를 발생시킨다")
    void shouldThrowExceptionForMalformedToken() {
        // given
        String malformedToken = "invalid.jwt.token";

        // when & then
        assertThat(org.assertj.core.api.Assertions.catchThrowable(
            () -> jwtDecoder.validateAndGetClaims(malformedToken)
        )).isInstanceOf(RuntimeException.class); // MalformedJwtException 또는 다른 예외
    }

    @Test
    @DisplayName("null 토큰은 IllegalArgumentException을 발생시킨다")
    void shouldThrowExceptionForNullToken() {
        // when & then
        assertThat(org.assertj.core.api.Assertions.catchThrowable(
            () -> jwtDecoder.validateAndGetClaims(null)
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
