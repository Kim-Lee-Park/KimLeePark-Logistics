package com.klp.common.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.common.TestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TestController.class)
@Import(SecurityConfig.class)
class PreAuthorizeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증되지 않은 사용자가 퍼블릭 엔드포인트 접근 성공")
    void guestUserTestEndPointSuccess() throws Exception {
        mockMvc.perform(get("/test"))
            .andExpect(status().isOk())
            .andExpect(content().string("test"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 관리자 엔드포인트 접근 실패")
    void guestUserAdminEndPointFail() throws Exception {
        mockMvc.perform(get("/test/admin"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자 사용자가 관리자 엔드포인트 접근 성공")
    void adminUserAdminEndPointFail() throws Exception {
        mockMvc.perform(get("/test/admin"))
            .andExpect(status().isOk())
            .andExpect(content().string("admin"));
    }
}
