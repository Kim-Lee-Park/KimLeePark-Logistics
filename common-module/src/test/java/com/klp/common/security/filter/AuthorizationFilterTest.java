package com.klp.common.security.filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.common.TestController;
import com.klp.common.security.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TestController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class})
class AuthorizationFilterTest {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("필수 헤더가 모두 존재 시, SecurityContextHolder에 인증 객체가 설정된다")
    void headerAllExist_thenSetAuthenticationObjectSuccess() throws Exception {
        String userId = "1";
        String userName = "testUser";
        String role = "MASTER";

        mockMvc.perform(get("/test/auth")
                .header(USER_ID_HEADER, userId)
                .header(USER_NAME_HEADER, userName)
                .header(USER_ROLE_HEADER, role))
            .andExpect(status().isOk())
            .andExpect(content().string(userId + "," + userName + "," + role));
    }

    @Test
    @DisplayName("필수 헤더 중 하나라도 없다면, SecurityContextHolder에 인증 객체가 설정되지 않는다")
    void missingHeaderExist_thenSetAuthenticationObjectFail() throws Exception {
        String userId = "1";
        String role = "MASTER";

        mockMvc.perform(get("/test/auth")
                .header(USER_ID_HEADER, userId)
                .header(USER_ROLE_HEADER, role))
            .andExpect(status().isOk())
            .andExpect(content().string("guest"));
    }
}
