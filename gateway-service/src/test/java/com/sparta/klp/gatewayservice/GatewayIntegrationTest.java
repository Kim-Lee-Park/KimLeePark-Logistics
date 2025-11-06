package com.sparta.klp.gatewayservice;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 8081)
@DisplayName("JWT 인증 필터 통합 테스트")
class GatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;


    private String testKey = "testSecretKeytestSecretKeytestSecretKey";
    private SecretKey key;

    private static final String PUBLIC_PATH = "/v1/test/public";
    private static final String AUTHENTICATED_PATH = "/v1/test/authenticated";

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String USER_ROLE_HEADER = "X-User-Role";


    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(testKey.getBytes(StandardCharsets.UTF_8));

        stubFor(get(urlEqualTo(PUBLIC_PATH))
            .willReturn(aResponse()
                .withStatus(200))
        );

        stubFor(get(urlEqualTo(AUTHENTICATED_PATH))
            .willReturn(aResponse()
                .withStatus(200))
        );
    }

    @Test
    @DisplayName("Public Path의 경우 토큰 없이도 접근할 수 있다")
    void publicPathWithoutTokenSuccess() {
        webTestClient.get()
            .uri(PUBLIC_PATH)
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    @DisplayName("Public Path가 아니라면 토큰 없이 접근이 불가능하다")
    void authenticatedPathWithoutTokenFail() {
        webTestClient.get()
            .uri(AUTHENTICATED_PATH)
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    @DisplayName("Bearer 형식의 토큰이 아니라면 401을 반환한다")
    void invalidTokenPrefix() {
        webTestClient.get()
            .uri(AUTHENTICATED_PATH)
            .header("Authorization", "token")
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    @DisplayName("유효한 토큰으로는 회원 전용 Path에 접근할 수 있다")
    void validTokenSuccess() {
        String userId = "1";
        String username = "테스트 유저";
        String role = "MASTER";
        String token = createValidToken(userId, username, role);

        webTestClient.get()
            .uri(AUTHENTICATED_PATH)
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk();

        verify(getRequestedFor(urlEqualTo(AUTHENTICATED_PATH))
            .withHeader(USER_ID_HEADER, equalTo(userId))
            .withHeader(USER_NAME_HEADER, equalTo(URLEncoder.encode(username, StandardCharsets.UTF_8)))
            .withHeader(USER_ROLE_HEADER, equalTo(role))
        );
    }

    @Test
    @DisplayName("만료된 토큰 사용 시 401을 반환한다")
    void expiredTokenFail() {
        String userId = "1";
        String username = "테스트 유저";
        String role = "MASTER";
        String token = createExpiredToken(userId, username, role);

        webTestClient.get()
            .uri(AUTHENTICATED_PATH)
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    @DisplayName("조작된 토큰 사용 시 401을 반환한다")
    void malformedTokenFail() {
        webTestClient.get()
            .uri(AUTHENTICATED_PATH)
            .header("Authorization", "Bearer " + "malformed.token.data")
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    @DisplayName("서명이 올바르지 않은 토큰 사용 시 401을 반환한다")
    void invalidSignatureTokenFail() {
        String userId = "1";
        String username = "테스트 유저";
        String role = "MASTER";
        String token = createInvalidSignatureToken(userId, username, role);

        webTestClient.get()
            .uri(AUTHENTICATED_PATH)
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    private String createValidToken(String userId, String username, String role) {
        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(key)
            .compact();
    }

    private String createExpiredToken(String userId, String username, String role) {
        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date(System.currentTimeMillis() - 7200000))
            .expiration(new Date(System.currentTimeMillis() - 3600000))
            .signWith(key)
            .compact();
    }

    private String createInvalidSignatureToken(String userId, String username, String role) {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
            "wrongSecretKeywrongSecretKeywrongSecretKey".getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(wrongKey)
            .compact();
    }
}
