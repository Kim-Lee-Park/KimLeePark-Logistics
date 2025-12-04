package com.klp.promotion.coupon.presentation.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.presentation.dto.IssueUserCouponResponse;
import com.klp.promotion.global.exception.GlobalExceptionHandler;
import com.klp.promotion.global.security.config.SecurityConfig;
import com.klp.promotion.global.security.filter.AuthorizationFilter;
import com.klp.promotion.global.security.model.UserDetailsImpl;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserCouponController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
@DisplayName("UserCouponController 테스트")
class UserCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCouponFacade userCouponFacade;

    @MockitoBean
    private UserCouponService userCouponService;

    @Test
    @DisplayName("권한이 있는 사용자가 쿠폰 발급 성공")
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void issueUserCoupon_Success_WithRole() throws Exception {
        // given
        UUID couponId = UUID.randomUUID();
        UUID userCouponId = UUID.randomUUID();
        Long userId = 1L;

        IssueUserCouponResponse response = new IssueUserCouponResponse(couponId, userCouponId);
        when(userCouponFacade.issueUserCoupon(eq(couponId), eq(userId))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/v1/user-coupons")
                .with(user(new UserDetailsImpl(userId, "testuser", "CUSTOMER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + couponId + "\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.couponId").value(couponId.toString()))
            .andExpect(jsonPath("$.userCouponId").value(userCouponId.toString()));
    }

    @Test
    @DisplayName("잘못된 권한으로 쿠폰 발급 실패")
    @WithMockUser(username = "testuser", roles = "DRIVER")
    void issueUserCoupon_Fail_InvalidRole() throws Exception {
        // given
        UUID couponId = UUID.randomUUID();

        // when & then - CUSTOMER 권한이 필요한데 DRIVER로 요청
        mockMvc.perform(post("/v1/user-coupons")
                .with(user(new UserDetailsImpl(1L, "testuser", "DRIVER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + couponId + "\""))
            .andExpect(status().isForbidden());
    }
}

