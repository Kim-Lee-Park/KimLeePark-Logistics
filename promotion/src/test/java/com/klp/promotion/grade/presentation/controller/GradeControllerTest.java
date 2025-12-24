package com.klp.promotion.grade.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.promotion.global.exception.BusinessException;
import com.klp.promotion.global.security.model.UserDetailsImpl;
import com.klp.promotion.grade.application.GradeService;
import com.klp.promotion.grade.application.dto.CreateGradeCommand;
import com.klp.promotion.grade.application.dto.UpdateGradeCommand;
import com.klp.promotion.grade.exception.GradeErrorCode;
import com.klp.promotion.grade.presentation.dto.request.CreateGradeRequest;
import com.klp.promotion.grade.presentation.dto.request.UpdateGradeRequest;
import com.klp.promotion.grade.presentation.dto.response.GradeResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GradeController.class)
@DisplayName("GradeController 테스트")
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GradeService gradeService;

    @Nested
    @DisplayName("등급 생성 API 테스트")
    class CreateGradeApiTest {

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("정상적으로 등급을 생성한다")
        void createGrade_Success() throws Exception {
            // given
            CreateGradeRequest request = new CreateGradeRequest("VIP", 15, 20000000L, 50000000L);
            GradeResponse response = new GradeResponse(UUID.randomUUID(), "VIP", 15, 20000000L,
                50000000L);

            when(gradeService.createGrade(any(CreateGradeCommand.class))).thenReturn(response);

            // when & then
            mockMvc.perform(post("/v1/promotions/grades")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeName").value("VIP"))
                .andExpect(jsonPath("$.benefitDiscountRate").value(15))
                .andExpect(jsonPath("$.minAmount").value(20000000))
                .andExpect(jsonPath("$.maxAmount").value(50000000));
        }

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("등급 이름이 없으면 400 에러를 반환한다")
        void createGrade_NoGradeName_ReturnsBadRequest() throws Exception {
            // given
            CreateGradeRequest request = new CreateGradeRequest("", 15, 20000000L, 50000000L);

            // when & then
            mockMvc.perform(post("/v1/promotions/grades")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("할인율이 음수이면 400 에러를 반환한다")
        void createGrade_NegativeDiscountRate_ReturnsBadRequest() throws Exception {
            // given
            CreateGradeRequest request = new CreateGradeRequest("VIP", -1, 20000000L, 50000000L);

            // when & then
            mockMvc.perform(post("/v1/promotions/grades")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("할인율이 100을 초과하면 400 에러를 반환한다")
        void createGrade_DiscountRateOver100_ReturnsBadRequest() throws Exception {
            // given
            CreateGradeRequest request = new CreateGradeRequest("VIP", 101, 20000000L, 50000000L);

            // when & then
            mockMvc.perform(post("/v1/promotions/grades")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("최소 금액이 음수이면 400 에러를 반환한다")
        void createGrade_NegativeMinAmount_ReturnsBadRequest() throws Exception {
            // given
            CreateGradeRequest request = new CreateGradeRequest("VIP", 15, -1L, 50000000L);

            // when & then
            mockMvc.perform(post("/v1/promotions/grades")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    @DisplayName("등급 조회 API 테스트")
    class GetGradeApiTest {

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("ID로 등급을 정상적으로 조회한다")
        void getGrade_Success() throws Exception {
            // given
            UUID gradeId = UUID.randomUUID();
            GradeResponse response = new GradeResponse(gradeId, "VIP", 15, 20000000L, 50000000L);

            when(gradeService.getGrade(gradeId)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/v1/promotions/grades/{gradeId}", gradeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeId").value(gradeId.toString()))
                .andExpect(jsonPath("$.gradeName").value("VIP"))
                .andExpect(jsonPath("$.benefitDiscountRate").value(15))
                .andExpect(jsonPath("$.minAmount").value(20000000))
                .andExpect(jsonPath("$.maxAmount").value(50000000));
        }

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("존재하지 않는 등급 조회 시 예외가 발생한다")
        void getGrade_NotFound_ThrowsException() throws Exception {
            // given
            UUID gradeId = UUID.randomUUID();
            when(gradeService.getGrade(gradeId)).thenThrow(
                new BusinessException(GradeErrorCode.GRADE_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/v1/promotions/grades/{gradeId}", gradeId))
                .andDo(print())
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("등급 목록 조회 API 테스트")
    class GetGradesApiTest {

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("모든 등급 목록을 정상적으로 조회한다")
        void getGrades_Success() throws Exception {
            // given
            List<GradeResponse> responses = Arrays.asList(
                new GradeResponse(UUID.randomUUID(), "Bronze", 4, 1000000L, 5000000L),
                new GradeResponse(UUID.randomUUID(), "Silver", 7, 5000000L, 10000000L),
                new GradeResponse(UUID.randomUUID(), "Gold", 10, 10000000L, 50000000L)
            );

            when(gradeService.getGrades()).thenReturn(responses);

            // when & then
            mockMvc.perform(get("/v1/promotions/grades"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].gradeName").value("Bronze"))
                .andExpect(jsonPath("$[1].gradeName").value("Silver"))
                .andExpect(jsonPath("$[2].gradeName").value("Gold"));
        }

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("등급이 없을 때 빈 배열을 반환한다")
        void getGrades_EmptyList() throws Exception {
            // given
            when(gradeService.getGrades()).thenReturn(List.of());

            // when & then
            mockMvc.perform(get("/v1/promotions/grades"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("등급 수정 API 테스트")
    class UpdateGradeApiTest {

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("등급을 정상적으로 수정한다")
        void updateGrade_Success() throws Exception {
            // given
            UUID gradeId = UUID.randomUUID();
            UpdateGradeRequest request = new UpdateGradeRequest("VVIP", 20, 50000000L, 100000000L);
            GradeResponse response = new GradeResponse(gradeId, "VVIP", 20, 50000000L, 100000000L);

            when(gradeService.updateGrade(eq(gradeId), any(UpdateGradeCommand.class))).thenReturn(
                response);

            // when & then
            mockMvc.perform(put("/v1/promotions/grades/{gradeId}", gradeId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeName").value("VVIP"))
                .andExpect(jsonPath("$.benefitDiscountRate").value(20))
                .andExpect(jsonPath("$.minAmount").value(50000000))
                .andExpect(jsonPath("$.maxAmount").value(100000000));
        }

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("존재하지 않는 등급 수정 시 예외가 발생한다")
        void updateGrade_NotFound_ThrowsException() throws Exception {
            // given
            UUID gradeId = UUID.randomUUID();
            UpdateGradeRequest request = new UpdateGradeRequest("VVIP", 20, 50000000L, 100000000L);

            when(gradeService.updateGrade(eq(gradeId), any(UpdateGradeCommand.class)))
                .thenThrow(new BusinessException(GradeErrorCode.GRADE_NOT_FOUND));

            // when & then
            mockMvc.perform(put("/v1/promotions/grades/{gradeId}", gradeId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "MASTER")
        @DisplayName("등급 이름이 없으면 400 에러를 반환한다")
        void updateGrade_NoGradeName_ReturnsBadRequest() throws Exception {
            // given
            UUID gradeId = UUID.randomUUID();
            UpdateGradeRequest request = new UpdateGradeRequest("", 20, 50000000L, 100000000L);

            // when & then
            mockMvc.perform(put("/v1/promotions/grades/{gradeId}", gradeId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("등급 삭제 API 테스트")
    class DeleteGradeApiTest {

        @Test
        @WithMockUser(username = "testuser", roles = "MASTER")
        @DisplayName("등급을 정상적으로 삭제한다")
        void deleteGrade_Success() throws Exception {
            // given
            UUID gradeId = UUID.randomUUID();
            Long userId = 1L;

            doNothing().when(gradeService).deleteGrade(eq(gradeId), eq(userId));

            // when & then
            mockMvc.perform(delete("/v1/promotions/grades/{gradeId}", gradeId)
                    .with(csrf())
                    .with(user(new UserDetailsImpl(userId, "testuser", "MASTER"))))
                .andDo(print())
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "MASTER")
        @DisplayName("존재하지 않는 등급 삭제 시 예외가 발생한다")
        void deleteGrade_NotFound_ThrowsException() throws Exception {
            // given
            UUID gradeId = UUID.randomUUID();
            Long userId = 1L;

            doThrow(new BusinessException(GradeErrorCode.GRADE_NOT_FOUND))
                .when(gradeService).deleteGrade(eq(gradeId), eq(userId));

            // when & then
            mockMvc.perform(delete("/v1/promotions/grades/{gradeId}", gradeId)
                    .with(csrf())
                    .with(user(new UserDetailsImpl(userId, "testuser", "MASTER"))))
                .andDo(print())
                .andExpect(status().isNotFound());
        }

    }
}
