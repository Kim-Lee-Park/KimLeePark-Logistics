package com.sparta.klp.gatewayservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GatewayProperties 설정 테스트")
class GatewayPropertiesTest {

    @Test
    @DisplayName("public-path를 올바르게 초기화한다")
    void singlePublicPathInit() {
        // given
        List<String> publicPaths = List.of("/v1/auth/signup");

        // when
        GatewayProperties properties = new GatewayProperties(publicPaths);

        // then
        assertThat(properties.publicPaths()).hasSize(1);
        assertThat(properties.publicPaths()).containsExactly("/v1/auth/signup");
    }

    @Test
    @DisplayName("여러 개의 public-paths를 올바르게 초기화한다")
    void multiPublicPathInit() {
        // given
        List<String> publicPaths = List.of(
            "/v1/auth/signup",
            "/v1/auth/login",
            "/v1/auth/logout"
        );

        // when
        GatewayProperties properties = new GatewayProperties(publicPaths);

        // then
        assertThat(properties.publicPaths()).isNotNull();
        assertThat(properties.publicPaths()).hasSize(3);
        assertThat(properties.publicPaths()).containsExactly(
            "/v1/auth/signup",
            "/v1/auth/login",
            "/v1/auth/logout"
        );
    }

    @Test
    @DisplayName("null이 전달되면 빈 리스트로 초기화한다")
    void shouldReturnEmptyWhenNullPath() {
        // when
        GatewayProperties properties = new GatewayProperties(null);

        // then
        assertThat(properties.publicPaths()).isNotNull();
        assertThat(properties.publicPaths()).isEmpty();
    }
}
