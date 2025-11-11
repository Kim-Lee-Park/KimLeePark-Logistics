package com.klp.authservice.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.authservice.auth.AuthErrorCode;
import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.application.command.LoginCommand;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.domain.entity.BlackListToken;
import com.klp.authservice.auth.domain.enums.AffiliationType;
import com.klp.authservice.auth.domain.repository.BlackListTokenRepository;
import com.klp.authservice.auth.entrypoint.dto.response.LoginResponse;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.authservice.auth.infrastructure.external.dto.response.UserDataDTO;
import com.klp.authservice.auth.infrastructure.jwt.TokenProvider;
import com.klp.common.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider accessTokenProvider;

    @Mock
    private BlackListTokenRepository blackListTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("SignUp 메소드 실패 테스트")
    class SignUpService_fail {

        @Test
        @DisplayName("회원 이름이 중복일 시 회원가입에 실패한다")
        void duplicationUserName_fail() {
            // given
            String userName = "testuser";
            String password = "Password1!";
            String slackId = " slackId";
            String affiliationName = "testCompany";
            AffiliationType affiliationType = AffiliationType.COMPANY;
            SignUpCommand command = new SignUpCommand(userName, password, slackId, affiliationName, affiliationType);

            // when
            when(userClient.checkUserNameAvailable(userName)).thenReturn(true);

            // then
            assertThatThrownBy(() -> authService.signUp(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.USERNAME_IS_EXIST.getMessage());

            verify(passwordEncoder, never()).encode(any());
            verify(userClient, never()).createUser(any(UserCreateRequest.class));
        }

        /**
         * 우선 호출 실패 시에 추가적인 작업이 이루어져야할 것으로 생각해 1차로 RuntimeException으로 처리. Timeout 처리 등에 관해서 적용 시 테스트 코드 내용 수정 필요
         */
        @Test
        @DisplayName("UserClient 호출 실패 시 예외가 발생한다")
        void userClientCall_fail() {
            // given
            String userName = "testuser";
            String password = "Password1!";
            String slackId = " slackId";
            String affiliationName = "testCompany";
            AffiliationType affiliationType = AffiliationType.COMPANY;
            SignUpCommand command = new SignUpCommand(userName, password, slackId, affiliationName, affiliationType);

            // when
            doThrow(new RuntimeException("UserClient 호출 오류"))
                .when(userClient).checkUserNameAvailable(any());

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
            String userName = "testuser";
            String password = "Password1!";
            String slackId = " slackId";
            String affiliationName = "testCompany";
            String encodedPassword = "encodedPassword";
            AffiliationType affiliationType = AffiliationType.COMPANY;
            SignUpCommand command = new SignUpCommand(userName, password, slackId, affiliationName, affiliationType);
            UserCreateRequest request = new UserCreateRequest(userName, encodedPassword, slackId, affiliationName,
                affiliationType);

            when(userClient.checkUserNameAvailable(userName)).thenReturn(false);
            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            doNothing().when(userClient).createUser(request);

            // when
            authService.signUp(command);

            // then
            verify(userClient).checkUserNameAvailable(userName);
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
            String userName = "testuser";
            String password = "Password1!";
            String encodedPassword = "encodedPassword";
            String role = "MASTER";
            String accessToken = "access.token.data";
            Long userId = 1L;

            LoginCommand command = new LoginCommand(userName, password);
            UserDataDTO dto = new UserDataDTO(userId, userName, encodedPassword, role);

            when(userClient.getUserByUserName(userName)).thenReturn(dto);
            when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
            when(accessTokenProvider.generate(userId, userName, role)).thenReturn(accessToken);

            // when
            LoginResponse response = authService.login(command);

            // then
            assertThat(response.userName()).isEqualTo(userName);
            assertThat(response.role()).isEqualTo(role);
            assertThat(response.accessToken()).isEqualTo(accessToken);

            verify(userClient).getUserByUserName(userName);
            verify(passwordEncoder).matches(password, encodedPassword);
            verify(accessTokenProvider).generate(userId, userName, role);

        }

        @Test
        @DisplayName("존재하지 않는 사용자는 로그인에 실패한다")
        void notExistUser_fail() {
            // given
            String userName = "notExistUser";
            String password = "Password1!";
            LoginCommand command = new LoginCommand(userName, password);

            // when
            when(userClient.getUserByUserName(userName))
                .thenThrow(new BusinessException(AuthErrorCode.USER_NOT_FOUND));

            // then
            assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.USER_NOT_FOUND.getMessage());

            verify(userClient).getUserByUserName(userName);
            verify(passwordEncoder, never()).matches(any(), any());
            verify(accessTokenProvider, never()).generate(any(), any(), any());
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
        void passwordMismatch_fail() {
            // given
            Long userId = 1L;
            String userName = "testuser";
            String password = "WrongPassword!";
            String encodedPassword = "encodedPassword";
            String role = "MASTER";

            LoginCommand command = new LoginCommand(userName, password);
            UserDataDTO userResponse = new UserDataDTO(userId, userName, encodedPassword, role);

            // when
            when(userClient.getUserByUserName(userName)).thenReturn(userResponse);
            when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

            // then
            assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.INVALID_PASSWORD.getMessage());

            verify(userClient).getUserByUserName(userName);
            verify(passwordEncoder).matches(password, encodedPassword);
            verify(accessTokenProvider, never()).generate(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Logout 메소드 테스트")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃에 성공한다. MASTER 유저의 토큰은 블랙리스트 토큰으로 등록된다")
        void logout_master_success() {
            // given
            String accessToken = "valid.access.token";
            String role = "MASTER";
            LocalDateTime expiration = LocalDateTime.now().plusHours(1);

            when(accessTokenProvider.validateToken(accessToken)).thenReturn(true);
            when(accessTokenProvider.getRole(accessToken)).thenReturn(role);
            when(accessTokenProvider.getExpiration(accessToken)).thenReturn(expiration);

            // when
            authService.logout(accessToken);

            // then
            verify(accessTokenProvider).getRole(accessToken);
            verify(accessTokenProvider).getExpiration(accessToken);
            verify(blackListTokenRepository).save(any(BlackListToken.class));
        }

        @Test
        @DisplayName("로그아웃에 성공한다. HUB 유저의 토큰은 블랙리스트 토큰으로 등록된다")
        void logout_hub_success() {
            // given
            String accessToken = "valid.access.token";
            String role = "HUB";
            LocalDateTime expiration = LocalDateTime.now().plusHours(1);

            when(accessTokenProvider.validateToken(accessToken)).thenReturn(true);
            when(accessTokenProvider.getRole(accessToken)).thenReturn(role);
            when(accessTokenProvider.getExpiration(accessToken)).thenReturn(expiration);

            // when
            authService.logout(accessToken);

            // then
            verify(blackListTokenRepository).save(any(BlackListToken.class));
        }

        @Test
        @DisplayName("로그아웃에 성공한다. MASTER, HUB 권한이 아닌 유저의 토큰은 블랙리스트 토큰으로 등록되지 않는다")
        void logout_normal_success() {
            // given
            String accessToken = "valid.access.token";
            String role = "COMPANY";

            when(accessTokenProvider.getRole(accessToken)).thenReturn(role);

            // when
            authService.logout(accessToken);

            // then
            verify(accessTokenProvider).getRole(accessToken);
            verify(blackListTokenRepository, never()).save(any());
            verify(accessTokenProvider, never()).getExpiration(any());
        }

        @Test
        @DisplayName("이미 블랙리스트에 있는 토큰은 중복 저장하지 않는다")
        void alreadyBlacklisted_fail() {
            // given
            String role = "MASTER";
            String accessToken = "valid.access.token";

            when(accessTokenProvider.validateToken(accessToken)).thenReturn(true);
            when(accessTokenProvider.getRole(accessToken)).thenReturn(role);
            when(blackListTokenRepository.existsByToken(accessToken)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.logout(accessToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.TOKEN_ALREADY_BLACKLISTED.getMessage());

            verify(blackListTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃 실패")
        void invalidToken_fail() {
            // given
            String invalidToken = "invalid.token";

            when(accessTokenProvider.validateToken(invalidToken))
                .thenThrow(new BusinessException(AuthErrorCode.INVALID_TOKEN));

            // when & then
            assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());
        }
    }
}
