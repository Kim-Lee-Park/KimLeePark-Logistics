package com.klp.promotion.grade.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.promotion.global.exception.GlobalExceptionHandler;
import com.klp.promotion.global.security.config.SecurityConfig;
import com.klp.promotion.global.security.filter.AuthorizationFilter;
import com.klp.promotion.grade.application.GradeService;
import com.klp.promotion.grade.presentation.dto.response.DefaultGradeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GradeInternalController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
@DisplayName("GradeInternalController 테스트")
class GradeInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GradeService gradeService;

    @Nested
    @DisplayName("기본 등급명 조회 API 테스트")
    class GetDefaultGradeApiTest {

        @Test
        @DisplayName("기본 등급명을 정상적으로 조회한다")
        void getDefaultGrade_Success() throws Exception {
            // given
            String defaultGradeName = "NONE";
            DefaultGradeResponse response = DefaultGradeResponse.of(defaultGradeName);

            when(gradeService.getDefaultGradeName()).thenReturn(response);

            // when & then
            mockMvc.perform(get("/v1/internal/promotions/grades/default"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeName").value(defaultGradeName));
        }

        @Test
        @DisplayName("기본 등급명이 'NONE'임을 확인한다")
        void getDefaultGrade_ReturnsNoneGrade() throws Exception {
            // given
            DefaultGradeResponse response = DefaultGradeResponse.of("NONE");

            when(gradeService.getDefaultGradeName()).thenReturn(response);

            // when & then
            mockMvc.perform(get("/v1/internal/promotions/grades/default"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeName").value("NONE"));
        }
    }
}
