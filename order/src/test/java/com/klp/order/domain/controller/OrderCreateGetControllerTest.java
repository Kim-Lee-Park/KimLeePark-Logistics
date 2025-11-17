package com.klp.order.domain.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.common.exception.BusinessException;
import com.klp.order.application.client.facade.OrderFacade;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.presentation.controller.OrderController;
import com.klp.order.presentation.dto.order.request.create.CreateOrderRequest;
import com.klp.order.presentation.dto.orderitem.request.OrderItemRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = OrderController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("OrderCreateGetController 테스트")
class OrderCreateGetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderFacade orderFacade;

    private CreateOrderRequest createOrderRequest;
    private Order savedOrder;
    private UUID productId1;
    private UUID productId2;
    private List<OrderItemCommand> itemCommands;

    @BeforeEach
    void setUp() {
        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();

        List<OrderItemRequest> orderItemRequests = List.of(
            new OrderItemRequest(productId1, 10),
            new OrderItemRequest(productId2, 5)
        );

        createOrderRequest = new CreateOrderRequest(
            1L,
            2L,
            "2025-11-05 14:00까지 납품 요청",
            orderItemRequests
        );

        itemCommands = List.of(
            new OrderItemCommand(productId1, 10),
            new OrderItemCommand(productId2, 5)
        );

    }

    private Order createOrder(Long supplierId, Long customerId, String comment,
        List<OrderItemCommand> command) {
        return Order.create(
            supplierId,
            customerId,
            comment,
            command
        );
    }

    @Test
    @DisplayName("주문 생성 - 정상")
    void createOrder_Success() throws Exception {
        // given
        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
        UUID testOrderId = UUID.randomUUID();
        ReflectionTestUtils.setField(savedOrder, "orderId", testOrderId);

        given(orderFacade.createOrder(any(CreateOrderCommand.class)))
            .willReturn(savedOrder);

        // when & then
        mockMvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.supplierId").value(1))
            .andExpect(jsonPath("$.customerId").value(2))
            .andExpect(jsonPath("$.comment").value("요구사항"))
            .andExpect(jsonPath("$.orderStatus").value("ING"))
            .andExpect(jsonPath("$.cancellation").isEmpty())
            .andExpect(jsonPath("$.orderItems").isArray())
            .andExpect(jsonPath("$.orderItems.length()").value(2))
            .andExpect(jsonPath("$.orderItems[0].productId").exists())
            .andExpect(jsonPath("$.orderItems[0].quantity").value(10))
            .andExpect(jsonPath("$.orderItems[0].deliveryId").isEmpty())
            .andExpect(jsonPath("$.orderItems[1].quantity").value(5));
    }

    @Test
    @DisplayName("주문 생성 - 실패 - supplierId null")
    void createOrder_Fail_SupplierIdNull() throws Exception {
        // given
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
            null,
            2L,
            "comment",
            createOrderRequest.orderItems()
        );

        // when & then
        mockMvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 생성 - 실패 - customerId null")
    void createOrder_Fail_CustomerIdNull() throws Exception {
        // given
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
            1L,
            null,
            "comment",
            createOrderRequest.orderItems()
        );

        // when & then
        mockMvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 생성 - 실패 - orderItems null")
    void createOrder_Fail_OrderItemsNull() throws Exception {
        // given
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
            1L,
            2L,
            "comment",
            null
        );

        // when & then
        mockMvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 생성 - 실패 - orderItems 빈 리스트")
    void createOrder_Fail_OrderItemsEmpty() throws Exception {
        // given
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
            1L,
            2L,
            "comment",
            List.of()
        );

        // when & then
        mockMvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 단일 조회 - 성공")
    void getOrders_Success() throws Exception {
        //given
        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
        UUID testOrderId = UUID.randomUUID();
        ReflectionTestUtils.setField(savedOrder, "orderId", testOrderId);

        UUID orderId = savedOrder.getOrderId();
        given(orderService.findById(orderId))
            .willReturn(savedOrder);

        //when&then
        mockMvc.perform(get("/v1/orders/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.supplierId").value(1))
            .andExpect(jsonPath("$.customerId").value(2))
            .andExpect(jsonPath("$.comment").value("요구사항"))
            .andExpect(jsonPath("$.orderStatus").value("ING"))
            .andExpect(jsonPath("$.cancellation").isEmpty())
            .andExpect(jsonPath("$.orderItems").isArray())
            .andExpect(jsonPath("$.orderItems.length()").value(2))
            .andExpect(jsonPath("$.orderItems[0].productId").exists())
            .andExpect(jsonPath("$.orderItems[0].quantity").value(10))
            .andExpect(jsonPath("$.orderItems[0].deliveryId").isEmpty())
            .andExpect(jsonPath("$.orderItems[1].quantity").value(5));
    }

    @Test
    @DisplayName("주문 단일 조회 - 실패 - 존재하지 않는 주문")
    void getOrder_Fail_OrderNotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        given(orderService.findById(nonExistentId))
            .willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/v1/orders/{orderId}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }


}
