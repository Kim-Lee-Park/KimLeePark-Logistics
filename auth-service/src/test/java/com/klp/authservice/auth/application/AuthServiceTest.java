package com.klp.authservice.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.application.command.LoginCommand;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.domain.entity.BlackListToken;
import com.klp.authservice.auth.domain.enums.AffiliationType;
import com.klp.authservice.auth.domain.repository.BlackListTokenRepository;
import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.authservice.auth.infrastructure.external.dto.response.UserDataResponse;
import com.klp.authservice.auth.infrastructure.external.dto.response.UsernameDuplicateResponse;
import com.klp.authservice.auth.infrastructure.jwt.TokenProvider;
import com.klp.authservice.auth.presentation.dto.response.LoginResponse;
import com.klp.authservice.auth.presentation.dto.response.ReissueResponse;
import com.klp.common.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private TokenProvider accessTokenProvider;

    @Mock
    private TokenProvider refreshTokenProvider;

    @Mock
    private BlackListTokenRepository blackListTokenRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userClient,
            accessTokenProvider,
            refreshTokenProvider,
            blackListTokenRepository
        );
    }

    @Nested
    @DisplayName("SignUp 메소드 실패 테스트")
    class SignUpService_fail {

        @Test
        @DisplayName("회원 이름이 중복일 시 회원가입에 실패한다")
        void duplicationUserName_fail() {
            // given
            String username = "testuser";
            String password = "Password1!";
            String slackId = " slackId";
            String phone = "010-1234-5678";
            String role = "MASTER";
            String affiliationName = "testCompany";
            AffiliationType affiliationType = AffiliationType.COMPANY;
            SignUpCommand command = new SignUpCommand(
                username, password, slackId, phone, role, affiliationName, affiliationType
            );

            // when
            UsernameDuplicateResponse dto = new UsernameDuplicateResponse(false);
            when(userClient.checkUsernameAvailable(username)).thenReturn(dto);

            // then
            assertThatThrownBy(() -> authService.signUp(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.USERNAME_IS_EXIST.getMessage());

            verify(userClient, never()).createUser(any(UserCreateRequest.class));
        }

        /**
         * 우선 호출 실패 시에 추가적인 작업이 이루어져야할 것으로 생각해 1차로 RuntimeException으로 처리. Timeout 처리 등에 관해서 적용 시 테스트 코드 내용 수정 필요
         */
        @Test
        @DisplayName("UserClient 호출 실패 시 예외가 발생한다")
        void userClientCall_fail() {
            // given
            String username = "testuser";
            String password = "Password1!";
            String slackId = " slackId";
            String phone = "010-1234-5678";
            String role = "MASTER";
            String affiliationName = "testCompany";
            AffiliationType affiliationType = AffiliationType.COMPANY;
            SignUpCommand command = new SignUpCommand(
                username, password, slackId, phone, role, affiliationName, affiliationType
            );

            // when
            doThrow(new RuntimeException("UserClient 호출 오류"))
                .when(userClient).checkUsernameAvailable(any());

            // then
            assertThatThrownBy(() -> authService.signUp(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("UserClient 호출 오류");
        }
    }

    @Nested
    @DisplayName("SignUp 메소드 성공 테스트")
    class SignUpService_success {

        @Test
        @DisplayName("회원가입에 성공한다")
        void signUp_success() {
            // given
            String username = "testuser";
            String password = "Password1!";
            String slackId = " slackId";
            String phone = "010-1234-5678";
            String role = "MASTER";
            String affiliationName = "testCompany";
            AffiliationType affiliationType = AffiliationType.COMPANY;

            SignUpCommand command = new SignUpCommand(
                username, password, slackId, phone, role, affiliationName, affiliationType
            );
            UserCreateRequest request = new UserCreateRequest(
                username, password, slackId, phone, role, affiliationName, affiliationType
            );

            UsernameDuplicateResponse dto = new UsernameDuplicateResponse(true);
            when(userClient.checkUsernameAvailable(username)).thenReturn(dto);
            doNothing().when(userClient).createUser(request);

            // when
            authService.signUp(command);

            // then
            verify(userClient).checkUsernameAvailable(username);
            verify(userClient).createUser(request);
        }
    }

    @Nested
    @DisplayName("Login 메소드 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인에 성공한다")
        void login_success() {
            // given
            String username = "testuser";
            String password = "Password1!";
            String encodedPassword = "encodedPassword";
            String role = "MASTER";
            String accessToken = "access.token.data";
            Long userId = 1L;

            LoginCommand command = new LoginCommand(username, password);
            UserDataResponse dto = new UserDataResponse(userId, username, encodedPassword, role);

            when(userClient.validateUserCredentials(username, password)).thenReturn(dto);
            when(accessTokenProvider.generate(userId, username, role)).thenReturn(accessToken);

            // when
            LoginResponse response = authService.login(command);

            // then
            assertThat(response.username()).isEqualTo(username);
            assertThat(response.role()).isEqualTo(role);
            assertThat(response.accessToken()).isEqualTo(accessToken);

            verify(userClient).validateUserCredentials(username, password);
            verify(accessTokenProvider).generate(userId, username, role);

        }

        @Test
        @DisplayName("존재하지 않는 사용자는 로그인에 실패한다")
        void notExistUser_fail() {
            // given
            String username = "notExistUser";
            String password = "Password1!";
            LoginCommand command = new LoginCommand(username, password);

            // when
            when(userClient.validateUserCredentials(username, password))
                .thenThrow(new BusinessException(AuthErrorCode.USER_NOT_FOUND));

            // then
            assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.USER_NOT_FOUND.getMessage());

            verify(userClient).validateUserCredentials(username, password);
            verify(accessTokenProvider, never()).generate(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Logout 메소드 테스트")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃에 성공한다. MASTER 유저의 AT, RT 모두 블랙리스트 토큰으로 등록된다")
        void logout_master_success() {
            // given
            String accessToken = "valid.access.token";
            String refreshToken = "valid.refresh.token";
            String role = "MASTER";
            LocalDateTime accessExpiration = LocalDateTime.now().plusHours(1);
            LocalDateTime refreshExpiration = LocalDateTime.now().plusDays(7);

            when(accessTokenProvider.validateToken(accessToken)).thenReturn(true);
            when(accessTokenProvider.getRole(accessToken)).thenReturn(role);
            when(blackListTokenRepository.existsByToken(accessToken)).thenReturn(false);
            when(accessTokenProvider.getExpiration(accessToken)).thenReturn(accessExpiration);

            when(blackListTokenRepository.existsByToken(refreshToken)).thenReturn(false);
            when(refreshTokenProvider.getExpiration(refreshToken)).thenReturn(refreshExpiration);

            // when
            authService.logout(accessToken, refreshToken);

            // then
            verify(accessTokenProvider).validateToken(accessToken);
            verify(accessTokenProvider).getRole(accessToken);
            verify(accessTokenProvider).getExpiration(accessToken);
            verify(refreshTokenProvider).getExpiration(refreshToken);
            verify(blackListTokenRepository, times(2)).save(any(BlackListToken.class));
        }

        @Test
        @DisplayName("로그아웃에 성공한다. HUB 유저의 AT, RT 모두 블랙리스트 토큰으로 등록된다")
        void logout_hub_success() {
            // given
            String accessToken = "valid.access.token";
            String refreshToken = "valid.refresh.token";
            String role = "HUB";
            LocalDateTime accessExpiration = LocalDateTime.now().plusHours(1);
            LocalDateTime refreshExpiration = LocalDateTime.now().plusDays(7);

            when(accessTokenProvider.validateToken(accessToken)).thenReturn(true);
            when(accessTokenProvider.getRole(accessToken)).thenReturn(role);
            when(blackListTokenRepository.existsByToken(accessToken)).thenReturn(false);
            when(accessTokenProvider.getExpiration(accessToken)).thenReturn(accessExpiration);

            when(blackListTokenRepository.existsByToken(refreshToken)).thenReturn(false);
            when(refreshTokenProvider.getExpiration(refreshToken)).thenReturn(refreshExpiration);

            // when
            authService.logout(accessToken, refreshToken);

            // then
            verify(blackListTokenRepository, times(2)).save(any(BlackListToken.class));
        }

        @Test
        @DisplayName("로그아웃에 성공한다. MASTER, HUB 권한이 아닌 유저의 토큰은 블랙리스트 토큰으로 등록되지 않는다")
        void logout_normal_success() {
            // given
            String accessToken = "valid.access.token";
            String refreshToken = "valid.refresh.token";
            String role = "COMPANY";

            when(accessTokenProvider.validateToken(accessToken)).thenReturn(true);
            when(accessTokenProvider.getRole(accessToken)).thenReturn(role);

            // when
            authService.logout(accessToken, refreshToken);

            // then
            verify(accessTokenProvider).validateToken(accessToken);
            verify(accessTokenProvider).getRole(accessToken);
            verify(blackListTokenRepository, never()).save(any());
            verify(accessTokenProvider, never()).getExpiration(any());
            verify(refreshTokenProvider, never()).getExpiration(any());
        }

        @Test
        @DisplayName("이미 블랙리스트에 있는 토큰은 중복 저장하지 않는다")
        void alreadyBlacklisted_success() {
            // given
            String accessToken = "valid.access.token";
            String refreshToken = "valid.refresh.token";
            String role = "MASTER";

            when(accessTokenProvider.validateToken(accessToken)).thenReturn(true);
            when(accessTokenProvider.getRole(accessToken)).thenReturn(role);
            when(blackListTokenRepository.existsByToken(accessToken)).thenReturn(true);
            when(blackListTokenRepository.existsByToken(refreshToken)).thenReturn(true);

            // when
            authService.logout(accessToken, refreshToken);

            // then
            verify(blackListTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃 실패")
        void invalidToken_fail() {
            // given
            String invalidToken = "invalid.token";
            String refreshToken = "valid.refresh.token";

            when(accessTokenProvider.validateToken(invalidToken)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.logout(invalidToken, refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    @Nested
    @DisplayName("Reissue 메소드 테스트")
    class ReissueTest {

        @Test
        @DisplayName("유효한 AT, RT로 새 AT 발급에 성공한다. 기존 AT, RT 모두 블랙리스트 등록")
        void reissue_with_both_tokens_success() {
            // given
            String accessToken = "old.access.token";
            String refreshToken = "valid.refresh.token";
            Long userId = 1L;
            String username = "testuser";
            String role = "MASTER";
            String newAccessToken = "new.access.token";
            LocalDateTime refreshExpiration = LocalDateTime.now().plusDays(7);
            LocalDateTime accessExpiration = LocalDateTime.now().plusHours(1);

            when(refreshTokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(blackListTokenRepository.existsByToken(refreshToken)).thenReturn(false);
            when(refreshTokenProvider.getUserId(refreshToken)).thenReturn(String.valueOf(userId));
            when(refreshTokenProvider.getUserName(refreshToken)).thenReturn(username);
            when(refreshTokenProvider.getRole(refreshToken)).thenReturn(role);
            when(refreshTokenProvider.getExpiration(refreshToken)).thenReturn(refreshExpiration);
            when(accessTokenProvider.generate(userId, username, role)).thenReturn(newAccessToken);

            when(blackListTokenRepository.existsByToken(accessToken)).thenReturn(false);
            when(accessTokenProvider.getExpiration(accessToken)).thenReturn(accessExpiration);

            // when
            ReissueResponse response = authService.reissue(accessToken, refreshToken);

            // then
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.username()).isEqualTo(username);
            assertThat(response.role()).isEqualTo(role);
            assertThat(response.accessToken()).isEqualTo(newAccessToken);

            verify(refreshTokenProvider).validateToken(refreshToken);
            verify(refreshTokenProvider).getUserId(refreshToken);
            verify(refreshTokenProvider).getUserName(refreshToken);
            verify(refreshTokenProvider).getRole(refreshToken);
            verify(refreshTokenProvider).getExpiration(refreshToken);
            verify(accessTokenProvider).getExpiration(accessToken);
            verify(blackListTokenRepository, times(2)).existsByToken(refreshToken);
            verify(blackListTokenRepository, times(1)).existsByToken(accessToken);
            verify(blackListTokenRepository, times(2)).save(any(BlackListToken.class));
            verify(accessTokenProvider).generate(userId, username, role);
        }

        @Test
        @DisplayName("AT 없이 RT만으로 새 AT 발급에 성공한다. RT만 블랙리스트 등록")
        void reissue_without_accessToken_success() {
            // given
            String refreshToken = "valid.refresh.token";
            Long userId = 1L;
            String username = "testuser";
            String role = "MASTER";
            String newAccessToken = "new.access.token";
            LocalDateTime refreshExpiration = LocalDateTime.now().plusDays(7);

            when(refreshTokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(blackListTokenRepository.existsByToken(refreshToken)).thenReturn(false);
            when(refreshTokenProvider.getUserId(refreshToken)).thenReturn(String.valueOf(userId));
            when(refreshTokenProvider.getUserName(refreshToken)).thenReturn(username);
            when(refreshTokenProvider.getRole(refreshToken)).thenReturn(role);
            when(refreshTokenProvider.getExpiration(refreshToken)).thenReturn(refreshExpiration);
            when(accessTokenProvider.generate(userId, username, role)).thenReturn(newAccessToken);

            // when
            ReissueResponse response = authService.reissue(null, refreshToken);

            // then
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.username()).isEqualTo(username);
            assertThat(response.role()).isEqualTo(role);
            assertThat(response.accessToken()).isEqualTo(newAccessToken);

            verify(refreshTokenProvider).validateToken(refreshToken);
            verify(refreshTokenProvider).getUserId(refreshToken);
            verify(refreshTokenProvider).getUserName(refreshToken);
            verify(refreshTokenProvider).getRole(refreshToken);
            verify(refreshTokenProvider).getExpiration(refreshToken);
            verify(blackListTokenRepository, times(2)).existsByToken(refreshToken);
            verify(blackListTokenRepository, times(1)).save(any(BlackListToken.class));
            verify(accessTokenProvider).generate(userId, username, role);
        }

        @Test
        @DisplayName("유효하지 않은 RT로는 새 토큰 발급에 실패한다")
        void invalidRefreshToken_fail() {
            // given
            String accessToken = "old.access.token";
            String invalidToken = "invalid.refresh.token";

            when(refreshTokenProvider.validateToken(invalidToken)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.reissue(accessToken, invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());
        }

        @Test
        @DisplayName("금지된 RT로는 새 토큰 발급에 실패한다")
        void blackListTokenExist_fail() {
            // given
            String accessToken = "old.access.token";
            String blacklistedToken = "blacklisted.refresh.token";

            when(refreshTokenProvider.validateToken(blacklistedToken)).thenReturn(true);
            when(blackListTokenRepository.existsByToken(blacklistedToken)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.reissue(accessToken, blacklistedToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.TOKEN_ALREADY_BLACKLISTED.getMessage());
        }
    }
}
