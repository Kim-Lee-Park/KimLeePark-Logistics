package com.klp.order.domain.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.facade.OrderFacade;
import com.klp.order.application.service.OrderService;
import com.klp.order.common.PageResponse;
import com.klp.order.common.PageableResponse;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.presentation.controller.OrderController;
import com.klp.order.presentation.dto.order.response.get.GetOrdersResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = OrderController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("OrderList Controller 테스트")
class OrderListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderFacade orderFacade;

    private Order order1;
    private Order order2;
    private List<OrderItemCommand> itemCommands;

    @BeforeEach
    void setUp() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        UUID hubId1 = UUID.randomUUID();
        UUID hubId2 = UUID.randomUUID();

        itemCommands = List.of(
            new OrderItemCommand(productId1, "상품명", hubId1, 10),
            new OrderItemCommand(productId2, "상품명", hubId2, 5)
        );

        order1 = Order.create(1L, 2L, "주문1", itemCommands);
        order2 = Order.create(2L, 3L, "주문2", itemCommands);

        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        ReflectionTestUtils.setField(order1, "orderId", orderId1);
        ReflectionTestUtils.setField(order2, "orderId", orderId2);
    }

    @Test
    @DisplayName("주문 목록 조회 - 정상 (필터 없음)")
    void getOrders_Success_NoFilters() throws Exception {
        // given
        List<GetOrdersResponse> data = List.of(
            GetOrdersResponse.from(order1),
            GetOrdersResponse.from(order2)
        );

        PageableResponse pageableResponse = new PageableResponse(
            0, 10, 2, 1, false, true, true
        );

        PageResponse<GetOrdersResponse> response = new PageResponse<>(data, pageableResponse);

        given(orderService.searchOrders(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].orderId").exists())
            .andExpect(jsonPath("$.data[0].supplierId").value(1))
            .andExpect(jsonPath("$.data[0].customerId").value(2))
            .andExpect(jsonPath("$.data[0].orderStatus").value("PENDING"))
            .andExpect(jsonPath("$.data[1].supplierId").value(2))
            .andExpect(jsonPath("$.data[1].customerId").value(3))
            .andExpect(jsonPath("$.pageable.page").value(0))
            .andExpect(jsonPath("$.pageable.size").value(10))
            .andExpect(jsonPath("$.pageable.totalElements").value(2))
            .andExpect(jsonPath("$.pageable.totalPages").value(1))
            .andExpect(jsonPath("$.pageable.hasNext").value(false))
            .andExpect(jsonPath("$.pageable.isFirst").value(true))
            .andExpect(jsonPath("$.pageable.isLast").value(true));
    }

    @Test
    @DisplayName("주문 목록 조회 - 정상 (supplierId 필터)")
    void getOrders_Success_WithSupplierFilter() throws Exception {
        // given
        List<GetOrdersResponse> data = List.of(
            GetOrdersResponse.from(order1)
        );

        PageableResponse pageableResponse = new PageableResponse(
            0, 10, 1, 1, false, true, true
        );

        PageResponse<GetOrdersResponse> response = new PageResponse<>(data, pageableResponse);

        given(orderService.searchOrders(
            eq(1L), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders")
                .param("supplierId", "1")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].supplierId").value(1))
            .andExpect(jsonPath("$.pageable.totalElements").value(1));
    }

    @Test
    @DisplayName("주문 목록 조회 - 정상 (customerId 필터)")
    void getOrders_Success_WithCustomerFilter() throws Exception {
        // given
        List<GetOrdersResponse> data = List.of(
            GetOrdersResponse.from(order1)
        );

        PageableResponse pageableResponse = new PageableResponse(
            0, 10, 1, 1, false, true, true
        );

        PageResponse<GetOrdersResponse> response = new PageResponse<>(data, pageableResponse);

        given(orderService.searchOrders(
            isNull(), eq(2L), isNull(), isNull(), isNull(), any(Pageable.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders")
                .param("customerId", "2")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].customerId").value(2));
    }

    @Test
    @DisplayName("주문 목록 조회 - 정상 (날짜 범위 필터)")
    void getOrders_Success_WithDateRangeFilter() throws Exception {
        // given
        List<GetOrdersResponse> data = List.of(
            GetOrdersResponse.from(order1),
            GetOrdersResponse.from(order2)
        );

        PageableResponse pageableResponse = new PageableResponse(
            0, 10, 2, 1, false, true, true
        );

        PageResponse<GetOrdersResponse> response = new PageResponse<>(data, pageableResponse);

        given(orderService.searchOrders(
            isNull(), isNull(), isNull(), any(LocalDate.class), any(LocalDate.class),
            any(Pageable.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("주문 목록 조회 - 정상 (모든 필터 적용)")
    void getOrders_Success_WithAllFilters() throws Exception {
        // given
        List<GetOrdersResponse> data = List.of(
            GetOrdersResponse.from(order1)
        );

        PageableResponse pageableResponse = new PageableResponse(
            0, 10, 1, 1, false, true, true
        );

        PageResponse<GetOrdersResponse> response = new PageResponse<>(data, pageableResponse);

        given(orderService.searchOrders(
            eq(1L), eq(2L), eq(100L),
            any(LocalDate.class), any(LocalDate.class),
            any(Pageable.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders")
                .param("supplierId", "1")
                .param("customerId", "2")
                .param("createdBy", "100")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].supplierId").value(1))
            .andExpect(jsonPath("$.data[0].customerId").value(2));
    }

    @Test
    @DisplayName("주문 목록 조회 - 정상 (빈 결과)")
    void getOrders_Success_EmptyResult() throws Exception {
        // given
        PageableResponse pageableResponse = new PageableResponse(
            0, 10, 0, 0, false, true, true
        );

        PageResponse<GetOrdersResponse> response = new PageResponse<>(List.of(), pageableResponse);

        given(orderService.searchOrders(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0))
            .andExpect(jsonPath("$.pageable.totalElements").value(0))
            .andExpect(jsonPath("$.pageable.totalPages").value(0));
    }

    @Test
    @DisplayName("주문 목록 조회 - 정상 (페이징 2페이지)")
    void getOrders_Success_SecondPage() throws Exception {
        // given
        List<GetOrdersResponse> data = List.of(
            GetOrdersResponse.from(order1)
        );

        PageableResponse pageableResponse = new PageableResponse(
            1, 10, 11, 2, false, false, true
        );

        PageResponse<GetOrdersResponse> response = new PageResponse<>(data, pageableResponse);

        given(orderService.searchOrders(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders")
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.pageable.page").value(1))
            .andExpect(jsonPath("$.pageable.hasNext").value(false))
            .andExpect(jsonPath("$.pageable.isFirst").value(false))
            .andExpect(jsonPath("$.pageable.isLast").value(true));
    }
}