package com.klp.order.domain.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.common.exception.BusinessException;
import com.klp.order.application.client.facade.OrderFacade;
import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.cancel.CancelType;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.presentation.controller.OrderController;
import com.klp.order.presentation.dto.order.request.cancel.CancelOrderRequest;
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
public class OrderCancelControllerTest {

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
    @DisplayName("주문 취소 - 정상")
    void cancelOrder_Success() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();
        Long cancelledBy = 100L;

        CancelOrderRequest cancelRequest = new CancelOrderRequest(
            "고객 요청으로 인한 주문 취소",
            CancelType.USER_REQUEST
        );

        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
        ReflectionTestUtils.setField(savedOrder, "orderId", testOrderId);
        savedOrder.cancel("고객 요청으로 인한 주문 취소", cancelledBy, CancelType.USER_REQUEST);

        given(orderService.cancelOrder(any(UUID.class), any(CancelOrderCommand.class)))
            .willReturn(savedOrder);

        // when & then
        mockMvc.perform(post("/v1/orders/" + testOrderId + "/cancel")
                .header("X-User-Id", cancelledBy.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(testOrderId.toString()))
            .andExpect(jsonPath("$.orderStatus").value("CANCELLED"))
            .andExpect(jsonPath("$.supplierId").value(1))
            .andExpect(jsonPath("$.customerId").value(2))
            .andExpect(jsonPath("$.cancellation").exists())
            .andExpect(jsonPath("$.cancellation.cancelReason").value("고객 요청으로 인한 주문 취소"))
            .andExpect(jsonPath("$.cancellation.cancelledBy").value(cancelledBy))
            .andExpect(jsonPath("$.cancellation.cancelType").value("USER_REQUEST"))
            .andExpect(jsonPath("$.cancellation.cancelledAt").exists());
    }

    @Test
    @DisplayName("주문 취소 - 실패 - 존재하지 않는 주문")
    void cancelOrder_Fail_OrderNotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        Long cancelledBy = 100L;

        CancelOrderRequest cancelRequest = new CancelOrderRequest(
            "고객 요청으로 인한 주문 취소",
            CancelType.USER_REQUEST
        );

        given(orderService.cancelOrder(any(UUID.class), any(CancelOrderCommand.class)))
            .willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/v1/orders/" + nonExistentId + "/cancel")
                .header("X-User-Id", cancelledBy.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 취소 - 실패 - cancelReason null")
    void cancelOrder_Fail_CancelReasonNull() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();
        Long cancelledBy = 100L;

        CancelOrderRequest invalidRequest = new CancelOrderRequest(
            null,
            CancelType.USER_REQUEST
        );

        // when & then
        mockMvc.perform(post("/v1/orders/" + testOrderId + "/cancel")
                .header("X-User-Id", cancelledBy.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 취소 - 실패 - cancelReason 빈 문자열")
    void cancelOrder_Fail_CancelReasonBlank() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();
        Long cancelledBy = 100L;

        CancelOrderRequest invalidRequest = new CancelOrderRequest(
            "",
            CancelType.USER_REQUEST
        );

        // when & then
        mockMvc.perform(post("/v1/orders/" + testOrderId + "/cancel")
                .header("X-User-Id", cancelledBy.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 취소 - 실패 - cancelType null")
    void cancelOrder_Fail_CancelTypeNull() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();
        Long cancelledBy = 100L;

        CancelOrderRequest invalidRequest = new CancelOrderRequest(
            "고객 요청으로 인한 주문 취소",
            null
        );

        // when & then
        mockMvc.perform(post("/v1/orders/" + testOrderId + "/cancel")
                .header("X-User-Id", cancelledBy.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 취소 - 실패 - 배송 할당된 주문")
    void cancelOrder_Fail_DeliveryAssigned() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();
        Long cancelledBy = 100L;

        CancelOrderRequest cancelRequest = new CancelOrderRequest(
            "고객 요청으로 인한 주문 취소",
            CancelType.USER_REQUEST
        );

        given(orderService.cancelOrder(any(UUID.class), any(CancelOrderCommand.class)))
            .willThrow(new BusinessException(OrderErrorCode.CANNOT_CANCEL_DELIVERY_ASSIGNED));

        // when & then
        mockMvc.perform(post("/v1/orders/" + testOrderId + "/cancel")
                .header("X-User-Id", cancelledBy.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 취소 - 실패 - 이미 취소된 주문")
    void cancelOrder_Fail_AlreadyCancelled() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();
        Long cancelledBy = 100L;

        CancelOrderRequest cancelRequest = new CancelOrderRequest(
            "고객 요청으로 인한 주문 취소",
            CancelType.USER_REQUEST
        );

        given(orderService.cancelOrder(any(UUID.class), any(CancelOrderCommand.class)))
            .willThrow(new BusinessException(OrderErrorCode.ALREADY_CANCELLED_ORDER));

        // when & then
        mockMvc.perform(post("/v1/orders/" + testOrderId + "/cancel")
                .header("X-User-Id", cancelledBy.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 취소 - 실패 - 완료된 주문")
    void cancelOrder_Fail_CompletedOrder() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();
        Long cancelledBy = 100L;

        CancelOrderRequest cancelRequest = new CancelOrderRequest(
            "고객 요청으로 인한 주문 취소",
            CancelType.USER_REQUEST
        );

        given(orderService.cancelOrder(any(UUID.class), any(CancelOrderCommand.class)))
            .willThrow(new BusinessException(OrderErrorCode.CANNOT_CANCEL_COMPLETED_ORDER));

        // when & then
        mockMvc.perform(post("/v1/orders/" + testOrderId + "/cancel")
                .header("X-User-Id", cancelledBy.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isBadRequest());
    }
}
