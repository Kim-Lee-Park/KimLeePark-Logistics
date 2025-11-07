package com.klp.authservice.auth.entrypoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.authservice.auth.application.AuthService;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.domain.enums.AffiliationType;
import com.klp.authservice.auth.entrypoint.controller.AuthController;
import com.klp.authservice.auth.entrypoint.dto.request.SignUpRequest;
import com.klp.authservice.auth.infrastructure.exception.GlobalExceptionHandler;
import com.klp.authservice.auth.infrastructure.security.config.SecurityConfig;
import com.klp.authservice.auth.infrastructure.security.filter.AuthorizationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

    @Nested
    @DisplayName("회원가입 실패 테스트")
    class failSignUp {

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
    class successSignUp {

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
}