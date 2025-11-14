package com.klp.delivery.routeplan.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.delivery.routeplan.application.service.RoutePlanService;
import com.klp.delivery.routeplan.fixture.RoutePlanFixture;
import com.klp.delivery.routeplan.presentation.controller.RoutePlanController;
import com.klp.delivery.routeplan.presentation.dto.request.CreateRoutePlanRequest;
import com.klp.delivery.routeplan.presentation.dto.response.CreateRoutePlanResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
            .andExpect(header().string("Location", "/v1/routes/plans/" + RoutePlanFixture.ROUTE_PLAN_ID))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.routePlanId").value(RoutePlanFixture.ROUTE_PLAN_ID.toString()));
    }
}
