package com.klp.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.global.exception.GlobalExceptionHandler;
import com.klp.global.security.config.SecurityConfig;
import com.klp.global.security.filter.AuthorizationFilter;
import com.klp.user.application.UserFacade;
import com.klp.user.application.UserService;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({UserController.class, UserInternalController.class})
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserFacade userFacade;

    @Nested
    @DisplayName("유저 이름 사용 가능 여부 확인 테스트")
    class heckUserNameDuplicateTest {

        @Test
        @DisplayName("성공 시 200 OK를 반환한다")
        void checkUserNameDuplication_success() throws Exception {
            // given
            final String username = "testuser";

            // when
            when(userService.checkUserNameAvailable(username)).thenReturn(any(UsernameCheckResponse.class));

            // then
            mockMvc.perform(get("/v1/users/check")
                    .param("username", username))
                .andExpect(status().isOk());
        }
    }
}
