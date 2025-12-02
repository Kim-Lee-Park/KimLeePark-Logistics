package com.klp.order.domain.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.application.facade.OrderFacade;
import com.klp.order.application.service.OrderService;
import com.klp.order.presentation.controller.OrderController;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = OrderController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("OrderProgress Controller 테스트")
class OrderProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderFacade orderFacade;

    @Test
    @DisplayName("진행 중인 주문 확인 - 진행 중인 주문이 있는 경우 (true)")
    void getOrderProgress_HasProgressingOrders_ReturnsTrue() throws Exception {
        // given
        UUID hubId = UUID.randomUUID();
        given(orderService.hasProgressingOrders(any(UUID.class)))
            .willReturn(true);

        // when & then
        mockMvc.perform(get("/v1/orders/progressing")
                .param("hubId", hubId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isOrderProgressing").value(true));
    }

    @Test
    @DisplayName("진행 중인 주문 확인 - 진행 중인 주문이 없는 경우 (false)")
    void getOrderProgress_NoProgressingOrders_ReturnsFalse() throws Exception {
        // given
        UUID hubId = UUID.randomUUID();
        given(orderService.hasProgressingOrders(any(UUID.class)))
            .willReturn(false);

        // when & then
        mockMvc.perform(get("/v1/orders/progressing")
                .param("hubId", hubId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isOrderProgressing").value(false));
    }

    @Test
    @DisplayName("진행 중인 주문 확인 - 모든 주문이 COMPLETE인 경우 (false)")
    void getOrderProgress_AllOrdersComplete_ReturnsFalse() throws Exception {
        // given
        UUID hubId = UUID.randomUUID();
        given(orderService.hasProgressingOrders(any(UUID.class)))
            .willReturn(false);

        // when & then
        mockMvc.perform(get("/v1/orders/progressing")
                .param("hubId", hubId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isOrderProgressing").value(false));
    }

    @Test
    @DisplayName("진행 중인 주문 확인 - hubId 파라미터 누락 시 400 에러")
    void getOrderProgress_MissingHubId_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/v1/orders/progressing")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

}
