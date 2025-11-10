package com.klp.authservice.auth.domain.entity;

import com.klp.authservice.auth.AuthErrorCode;
import com.klp.common.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlackListTokenTest {

    @Test
    @DisplayName("블랙리스트 토큰 생성 성공")
    void createBlackListToken_success() {
        // given
        String token = "blackList.token.data";
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);

        // when
        BlackListToken blackListToken = BlackListToken.create(token, expiration);

        // then
        assertThat(blackListToken.getToken()).isEqualTo(token);
        assertThat(blackListToken.getExpiration()).isEqalTo(expiration);
    }

    @Test
    @DisplayName("만료시간이 현재보다 이전이면 생성되지 않는다.")
    void createWithPastExpiration_fail() {
        // given
        String token = "blackList.token.data";
        LocalDateTime expiration = LocalDateTime.now().minusHours(1);

        // when & then
        assertThatThrownBy(() -> BlackListToken.create(token, expiration))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.BLACKLIST_CREATE_ERROR.getMessage());
    }

    @Test
    @DisplayName("만료시간이 null이라면 생성 실패한다")
    void nullExpiration_fail() {
        // given
        String token = "blackList.token.data";
        LocalDateTime expiration = null;

        // when & then
        assertThatThrownBy(() -> BlackListToken.create(token, expiration))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.BLACKLIST_CREATE_ERROR.getMessage());
    }

    @Test
    @DisplayName("토큰값이 null이라면 생성 실패한다.")
    void nullToken_fail() {
        // given
        String token = null;
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);

        // when & then
        assertThatThrownBy(() -> BlackListToken.create(token, expiration))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.BLACKLIST_CREATE_ERROR.getMessage());
    }

    @Test
    @DisplayName("토큰이 빈 문자열이라면 생성 실패한다.")
    void emptyToken_fail() {
        // given
        String token = "";
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);

        // when & then
        assertThatThrownBy(() -> BlackListToken.create(token, expiration))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.BLACKLIST_CREATE_ERROR.getMessage());
    }

    @Test
    @DisplayName("토큰이 공백이라면 생성 실패한다")
    void whiteSpaceToken_fail() {
        // given
        String token = "   ";
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);

        // when & then
        assertThatThrownBy(() -> BlackListToken.create(token, expiration))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.BLACKLIST_CREATE_ERROR.getMessage());
    }
}
