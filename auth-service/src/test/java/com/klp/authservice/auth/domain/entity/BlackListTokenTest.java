package com.klp.authservice.auth.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.authservice.global.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BlackListTokenTest {

    private static final String VALID_TOKEN = "blackList.token.data";
    private static final LocalDateTime VALID_EXPIRATION = LocalDateTime.now().plusHours(1);

    @Test
    @DisplayName("블랙리스트 토큰 생성 성공")
    void createBlackListToken_success() {
        // given

        // when
        BlackListToken blackListToken = BlackListToken.create(VALID_TOKEN, VALID_EXPIRATION);

        // then
        assertThat(blackListToken.getToken()).isEqualTo(VALID_TOKEN);
        assertThat(blackListToken.getExpiration()).isEqualTo(VALID_EXPIRATION);
    }

    @Test
    @DisplayName("만료시간이 현재보다 이전이면 생성되지 않는다.")
    void createWithPastExpiration_fail() {
        // given
        LocalDateTime invalidExpiration = LocalDateTime.now().minusHours(1);

        // when & then
        assertThatThrownBy(() -> BlackListToken.create(VALID_TOKEN, invalidExpiration))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.BLACKLIST_CREATE_ERROR.getMessage());
    }

    @Test
    @DisplayName("만료시간이 null이라면 생성 실패한다")
    void nullExpiration_fail() {
        // given

        // when & then
        assertThatThrownBy(() -> BlackListToken.create(VALID_TOKEN, null))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.BLACKLIST_CREATE_ERROR.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("토큰값이 null이거나 공백, 빈 문자열이면 생성에 실패한다.")
    void invalidToken_fail(String token) {
        // given

        // when & then
        assertThatThrownBy(() -> BlackListToken.create(token, VALID_EXPIRATION))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.BLACKLIST_CREATE_ERROR.getMessage());
    }
}
