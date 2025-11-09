package com.klp.authservice.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.authservice.auth.AuthErrorCode;
import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.domain.enums.AffiliationType;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.common.exception.BusinessException;
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
}
