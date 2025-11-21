package com.klp.delivery.routeplan.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.delivery.routeplan.application.service.RoutePlanService;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.fixture.RoutePlanFixture;
import com.klp.delivery.routeplan.presentation.controller.RoutePlanController;
import com.klp.delivery.routeplan.presentation.dto.request.CreateRoutePlanRequest;
import com.klp.delivery.routeplan.presentation.dto.response.CreateRoutePlanResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanListResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RoutePlanController.class)
public class RoutePlanControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    RoutePlanService routePlanService;

    private static final String BASE_URL = "/v1/routes";

    @Test
    @DisplayName("경로 계획 생성: 201 Created, Location 헤더, 응답 바디 반환")
    void createRoutePlan_success() throws Exception {
        // given
        given(routePlanService.createRoutePlan(any()))
            .willReturn(new CreateRoutePlanResponse(RoutePlanFixture.ROUTE_PLAN_ID));

        CreateRoutePlanRequest request = new CreateRoutePlanRequest(
            RoutePlanFixture.DEPARTURE_ID,
            RoutePlanFixture.ARRIVAL_ID
        );

        // when & then
        mockMvc.perform(post(BASE_URL + "/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.routePlanId").value(RoutePlanFixture.ROUTE_PLAN_ID.toString()));
    }

    @Test
    @DisplayName("출발 ID, 도착 ID로 조회")
    void get_byDepartureIdAndArrivalId_success() throws Exception {
        // given
        given(routePlanService.getRoutePlan(any(), any()))
            .willReturn(GetRoutePlanDetailResponse.from(RoutePlanFixture.createRoutePlan()));

        UUID depId = RoutePlanFixture.DEPARTURE_ID;
        UUID arrId = RoutePlanFixture.ARRIVAL_ID;
        // when

        // then
        mockMvc.perform(get(BASE_URL + "/plans/" + depId + "/" + arrId))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("경로 계획 ID로 조회")
    void get_byRoutePlanId_success() throws Exception {
        // given
        given(routePlanService.getRoutePlan(any()))
            .willReturn(GetRoutePlanDetailResponse.from(RoutePlanFixture.createRoutePlan()));

        UUID routePlanId = RoutePlanFixture.ROUTE_PLAN_ID;
        // when

        // then
        mockMvc.perform(get(BASE_URL + "/plans/" + routePlanId))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("경로 계획 목록 조회")
    void get_all_success() throws Exception {
        // given
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        Pageable pageable = PageRequest.of(0, 10);
        given(routePlanService.getRoutePlans(any(), any(), any())).willReturn(
            GetRoutePlanListResponse.from(new PageImpl<>(List.of(routePlan), pageable, 1)));
        // when

        // then
        mockMvc.perform(get(BASE_URL + "/plans")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("경로 계획 삭제")
    void deleteRoutePlan_success() throws Exception {
        // given
        UUID routePlanId = RoutePlanFixture.ROUTE_PLAN_ID;
        willDoNothing()
            .given(routePlanService)
            .deleteRoutePlan(any(UUID.class));   // or eq(routePlanId)

        // when & then
        mockMvc.perform(delete(BASE_URL + "/plans/" + routePlanId.toString()))
            .andExpect(status().is2xxSuccessful());
    }
}
