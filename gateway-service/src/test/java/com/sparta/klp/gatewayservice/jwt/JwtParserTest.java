package com.sparta.klp.gatewayservice.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JwtParser 단위 테스트")
class JwtParserTest {

    @Nested
    @DisplayName("extractTokenFromHeader 메소드는")
    class ExtractTokenFromHeaderTest {

        @Test
        @DisplayName("Bearer 토큰을 올바르게 추출한다")
        void extractTokenFromHeaderSuccess() {
            // given
            String authHeader = "Bearer testtokendata";

            // when
            Optional<String> result = JwtParser.extractTokenFromHeader(authHeader);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("testtokendata");
        }

        @Test
        @DisplayName("Authorization 헤더가 null이면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenHeaderIsNull() {
            // when
            Optional<String> result = JwtParser.extractTokenFromHeader(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Bearer 접두사가 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNoBearerPrefix() {
            // given
            String authHeader = "InvalidToken";

            // when
            Optional<String> result = JwtParser.extractTokenFromHeader(authHeader);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Bearer 뒤에 토큰이 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenTokenIsEmpty() {
            // given
            String authHeader = "Bearer ";

            // when
            Optional<String> result = JwtParser.extractTokenFromHeader(authHeader);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Bearer만 있고 공백도 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenOnlyBearer() {
            // given
            String authHeader = "Bearer";

            // when
            Optional<String> result = JwtParser.extractTokenFromHeader(authHeader);

            // then
            assertThat(result).isEmpty();
        }
    }
}
