package com.klp.authservice.auth.entrypoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.authservice.auth.AuthErrorCode;
import com.klp.authservice.auth.application.AuthService;
import com.klp.authservice.auth.application.command.LoginCommand;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.domain.enums.AffiliationType;
import com.klp.authservice.auth.entrypoint.controller.AuthController;
import com.klp.authservice.auth.entrypoint.dto.request.LoginRequest;
import com.klp.authservice.auth.entrypoint.dto.request.SignUpRequest;
import com.klp.authservice.auth.entrypoint.dto.response.LoginResponse;
import com.klp.authservice.auth.entrypoint.dto.response.ReissueResponse;
import com.klp.authservice.auth.infrastructure.exception.GlobalExceptionHandler;
import com.klp.authservice.auth.infrastructure.jwt.JwtConstants;
import com.klp.authservice.auth.infrastructure.jwt.TokenProvider;
import com.klp.authservice.auth.infrastructure.security.config.SecurityConfig;
import com.klp.authservice.auth.infrastructure.security.filter.AuthorizationFilter;
import com.klp.common.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected ObjectMapper mapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenProvider refreshTokenProvider;

    @Nested
    @DisplayName("회원가입 실패 테스트")
    class FailSignUp {

        @Nested
        @DisplayName("username 실패 케이스")
        class usernameInvalid {

            @Test
            @DisplayName("4글자 미만이면 실패한다")
            void underFourDigit_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("abc", "!Password123", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("10글자를 초과하면 실패한다")
            void overTenDigit_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("abcdefghijk", "!Password123", "slackId",
                    "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("대문자가 포함되면 실패한다")
            void upperCase_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("Testuser", "!Password123", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("특수문자가 포함되면 실패한다")
            void specialCharacter_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("testuser!", "!Password123", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("공백이 포함되면 실패한다")
            void whiteSpace_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("test user", "!Password123", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("null이면 실패한다")
            void nullUsername_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest(null, "!Password123", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("빈 문자열이면 실패한다")
            void emptyUsername_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("", "!Password123", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("공백이면 실패한다")
            void onlyWhiteSpaceUsername_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("   ", "!Password123", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("password 실패 케이스")
        class passwordInvalid {

            @Test
            @DisplayName("8글자 미만이면 실패한다")
            void under8Digit_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Pass1!", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("15글자를 초과하면 실패한다")
            void over15Digit_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password12345667!@#", "slackId",
                    "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("대문자가 없으면 실패한다")
            void upperCaseNotExist_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "password123!", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("소문자가 없으면 실패한다")
            void lowerCaseNotExist_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "PASSWORD123!", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("숫자가 없으면 실패한다")
            void numberNotExist_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password!@#!@#", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("특수문자가 없으면 실패한다")
            void specialCharNotExist_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password123123", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("null이면 실패한다")
            void nullPassword_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", null, "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("빈 문자열이면 실패한다")
            void emptyPassword_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("공백이면 실패한다")
            void onlyWhiteSpacePassword_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "   ", "slackId", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("slackId 실패 케이스")
        class slackIdInvalid {

            @Test
            @DisplayName("null이면 실패한다")
            void nullSlackId_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password1!", null, "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("빈 문자열이면 실패한다")
            void emptySlackId_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password1!", "", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("공백이면 실패한다")
            void whiteSpaceSlackId_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password1!", "  ", "testCopmpany",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("affiliationName 실패 케이스")
        class affiliationNameInvalid {

            @Test
            @DisplayName("null이면 실패한다")
            void nullAffiliationName_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password1!", "slackId", null,
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("빈 문자열이면 실패한다")
            void emptyAffiliationName_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password1!", "slackId", "",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("공백이면 실패한다")
            void whiteSpaceAffiliationName_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password1!", "slackId", "  ",
                    AffiliationType.COMPANY);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("affiliationType 실패 케이스")
        class affiliationTypeInvalid {

            @Test
            @DisplayName("null이면 실패한다")
            void nullAffiliationType_fail() throws Exception {
                // given
                SignUpRequest signUpRequest = new SignUpRequest("user", "Password1!", "slackId", "testCopmpany",
                    null);

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
            }

            /**
             * RequestDto 검증에서 enum 클래스 타입은 객체 생성보다는 json 직접 작성으로 작성
             */
            @Test
            @DisplayName("COMPANY, HUB가 아니라면 실패한다")
            void emptyAffiliationType_fail() throws Exception {
                // given
                String request = """
                    {
                        "userName": "user",
                        "password": "Password1!",
                        "slackId": "slackId",
                        "affiliationName": "testCompany",
                        "affiliationType": "TEST"
                    }
                    """;

                // when & then
                mockMvc.perform(post("/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("회원가입 성공 테스트")
    class SuccessSignUp {

        @Test
        @DisplayName("유효한 요청일 경우 회원가입에 성공한다")
        void validPassword_success() throws Exception {
            // given
            String userName = "testuser1";
            String password = "Password123!";
            String slackId = "slackId";
            String affiliationName = "testCompany";
            AffiliationType type = AffiliationType.COMPANY;

            SignUpRequest signUpRequest = new SignUpRequest(userName, password, slackId, affiliationName, type);

            // when
            doNothing().when(authService).signUp(any(SignUpCommand.class));

            // then
            mockMvc.perform(post("/v1/auth/signUp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("로그인 성공 테스트")
    class LoginSuccessTest {

        @Test
        @DisplayName("유효한 요청일 경우 로그인에 성공한다. 로그인 성공 시 쿠키로 RefreshToken이 발급된다.")
        void validRequest_success() throws Exception {
            // given
            Long userId = 1L;
            String userName = "testuser";
            String password = "Password1!";
            String role = "MASTER";
            String accessToken = "valid.access.token";
            String refreshToken = "valid.refresh.token";

            LoginRequest request = new LoginRequest(userName, password);
            LoginResponse response = new LoginResponse(userId, userName, role, accessToken);

            // when
            when(authService.login(any(LoginCommand.class))).thenReturn(response);
            when(refreshTokenProvider.generate(userId, userName, role)).thenReturn(refreshToken);

            // then
            mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(JwtConstants.REFRESH_TOKEN_COOKIE_NAME))
                .andExpect(cookie().value(JwtConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken));
        }
    }

    @Nested
    @DisplayName("로그인 실패 테스트")
    class LoginFailTest {

        @Nested
        @DisplayName("userName 실패 케이스")
        class UsernameInvalid {

            @Test
            @DisplayName("null이면 실패한다")
            void nullUsername_fail() throws Exception {
                // given
                LoginRequest loginRequest = new LoginRequest(null, "Password1!");

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("빈 문자열이면 실패한다")
            void emptyUsername_fail() throws Exception {
                // given
                LoginRequest loginRequest = new LoginRequest("", "Password1!");

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("공백이면 실패한다")
            void whiteSpaceUsername_fail() throws Exception {
                // given
                LoginRequest loginRequest = new LoginRequest("     ", "Password1!");

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("password 실패 케이스")
        class PasswordInvalid {

            @Test
            @DisplayName("null이면 실패한다")
            void nullPassword_fail() throws Exception {
                // given
                LoginRequest loginRequest = new LoginRequest("testuser", null);

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("빈 문자열이면 실패한다")
            void emptyPassword_fail() throws Exception {
                // given
                LoginRequest loginRequest = new LoginRequest("testuser", "");

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("공백이면 실패한다")
            void whiteSpacePassword_fail() throws Exception {
                // given
                LoginRequest loginRequest = new LoginRequest("testuser", "   ");

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("로그아웃 테스트")
    class logoutTest {

        @Test
        @DisplayName("로그아웃에 성공하고 RefreshToken 쿠키가 무효화된다")
        void logout_success() throws Exception {
            // given
            String accessToken = "valid.access.token";

            doNothing().when(authService).logout(accessToken);

            // when & then
            mockMvc.perform(post("/v1/auth/logout")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(JwtConstants.REFRESH_TOKEN_COOKIE_NAME, 0));
        }

        @Test
        @DisplayName("Authorization 헤더가 없으면 실패한다")
        void noAuthorizationHeader_fail() throws Exception {
            // when & then
            mockMvc.perform(post("/v1/auth/logout"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 실패한다")
        void invalidToken_fail() throws Exception {
            // given
            String invalidToken = "invalid.token";

            doThrow(new BusinessException(AuthErrorCode.INVALID_TOKEN))
                .when(authService).logout(invalidToken);

            // when & then
            mockMvc.perform(post("/v1/auth/logout")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class reissueTest {

        @Test
        @DisplayName("RefreshToken으로 새 AccessToken 발급에 성공한다")
        void reissue_success() throws Exception {
            // given
            String refreshToken = "valid.refresh.token";
            Long userId = 1L;
            String userName = "testuser";
            String role = "MASTER";
            String newAccessToken = "new.access.token";
            String newRefreshToken = "new.refresh.token";

            ReissueResponse response = new ReissueResponse(userId, userName, role, newAccessToken);

            when(authService.reissue(refreshToken)).thenReturn(response);
            when(refreshTokenProvider.generate(userId, userName, role)).thenReturn(newRefreshToken);

            // when & then
            mockMvc.perform(post("/v1/auth/token/reissue")
                    .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.userName").value(userName))
                .andExpect(jsonPath("$.role").value(role))
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(cookie().exists(JwtConstants.REFRESH_TOKEN_COOKIE_NAME));

            verify(authService).reissue(refreshToken);
            verify(refreshTokenProvider).generate(userId, userName, role);
        }

        @Test
        @DisplayName("RefreshToken 쿠키가 없으면 실패한다")
        void noRefreshTokenCookie_fail() throws Exception {
            // when & then
            mockMvc.perform(post("/v1/auth/token/reissue"))
                .andExpect(status().isInternalServerError());  // NullPointerException 발생
        }

        @Test
        @DisplayName("유효하지 않은 RefreshToken으로 실패한다")
        void invalidRefreshToken_fail() throws Exception {
            // given
            String invalidToken = "invalid.refresh.token";

            when(authService.reissue(invalidToken))
                .thenThrow(new BusinessException(AuthErrorCode.INVALID_TOKEN));

            // when & then
            mockMvc.perform(post("/v1/auth/token/reissue")
                    .cookie(new Cookie("refreshToken", invalidToken)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("블랙리스트 토큰으로 실패한다")
        void blacklistedToken_fail() throws Exception {
            // given
            String blacklistedToken = "blacklisted.refresh.token";

            when(authService.reissue(blacklistedToken))
                .thenThrow(new BusinessException(AuthErrorCode.TOKEN_ALREADY_BLACKLISTED));

            // when & then
            mockMvc.perform(post("/v1/auth/token/reissue")
                    .cookie(new Cookie("refreshToken", blacklistedToken)))
                .andExpect(status().isUnauthorized());
        }
    }
}