package com.klp.order.domain.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.command.UpdateOrderCommand;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.presentation.controller.OrderController;
import com.klp.order.presentation.dto.order.request.create.CreateOrderRequest;
import com.klp.order.presentation.dto.order.request.update.UpdateOrderRequest;
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
public class OrderUpdateDeleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

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
    @DisplayName("주문 수정 - 정상")
    void updateOrder_Success() throws Exception {
        // given
        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
        UUID testOrderId = UUID.randomUUID();

        List<OrderItemRequest> updatedOrderItemRequests = List.of(
            new OrderItemRequest(productId1, 15),
            new OrderItemRequest(productId2, 8)
        );

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            "2025-11-06 10:00까지 납품 요청으로 변경",
            updatedOrderItemRequests
        );

        List<OrderItemCommand> updatedItemCommands = List.of(
            new OrderItemCommand(productId1, 15),
            new OrderItemCommand(productId2, 8)
        );

        Order updatedOrder = createOrder(1L, 2L, "2025-11-06 10:00까지 납품 요청으로 변경",
            updatedItemCommands);
        ReflectionTestUtils.setField(updatedOrder, "orderId", testOrderId);

        given(orderService.updateOrder(any(UUID.class), any(UpdateOrderCommand.class)))
            .willReturn(updatedOrder);

        // when & then
        mockMvc.perform(patch("/v1/orders/" + testOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(testOrderId.toString()))
            .andExpect(jsonPath("$.comment").value("2025-11-06 10:00까지 납품 요청으로 변경"))
            .andExpect(jsonPath("$.orderStatus").value("ING"))
            .andExpect(jsonPath("$.cancellation").isEmpty())
            .andExpect(jsonPath("$.orderItems").isArray())
            .andExpect(jsonPath("$.orderItems.length()").value(2))
            .andExpect(jsonPath("$.orderItems[0].quantity").value(15))
            .andExpect(jsonPath("$.orderItems[1].quantity").value(8));
    }

    @Test
    @DisplayName("주문 수정 - 실패 - 존재하지 않는 주문")
    void updateOrder_Fail_OrderNotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();

        List<OrderItemRequest> updatedOrderItemRequests = List.of(
            new OrderItemRequest(productId1, 15)
        );

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            "수정된 요청사항",
            updatedOrderItemRequests
        );

        given(orderService.updateOrder(any(UUID.class), any(UpdateOrderCommand.class)))
            .willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(patch("/v1/orders/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 수정 - 실패 - orderItems null")
    void updateOrder_Fail_OrderItemsNull() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();

        UpdateOrderRequest invalidRequest = new UpdateOrderRequest(
            "수정된 요청사항",
            null
        );

        // when & then
        mockMvc.perform(patch("/v1/orders/" + testOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 수정 - 실패 - orderItems 빈 리스트")
    void updateOrder_Fail_OrderItemsEmpty() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();

        UpdateOrderRequest invalidRequest = new UpdateOrderRequest(
            "수정된 요청사항",
            List.of()
        );

        // when & then
        mockMvc.perform(patch("/v1/orders/" + testOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 수정 - 실패 - 배송 할당된 주문")
    void updateOrder_Fail_DeliveryAssigned() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();

        List<OrderItemRequest> updatedOrderItemRequests = List.of(
            new OrderItemRequest(productId1, 15)
        );

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            "수정 시도",
            updatedOrderItemRequests
        );

        given(orderService.updateOrder(any(UUID.class), any(UpdateOrderCommand.class)))
            .willThrow(new BusinessException(OrderErrorCode.CANNOT_UPDATE_DELIVERY_ASSIGNED));

        // when & then
        mockMvc.perform(patch("/v1/orders/" + testOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 수정 - 실패 - 취소된 주문")
    void updateOrder_Fail_CancelledOrder() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();

        List<OrderItemRequest> updatedOrderItemRequests = List.of(
            new OrderItemRequest(productId1, 15)
        );

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            "수정 시도",
            updatedOrderItemRequests
        );

        given(orderService.updateOrder(any(UUID.class), any(UpdateOrderCommand.class)))
            .willThrow(new BusinessException(OrderErrorCode.CANNOT_UPDATE_CANCELLED_ORDER));

        // when & then
        mockMvc.perform(patch("/v1/orders/" + testOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 수정 - 실패 - 완료된 주문")
    void updateOrder_Fail_CompletedOrder() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();

        List<OrderItemRequest> updatedOrderItemRequests = List.of(
            new OrderItemRequest(productId1, 15)
        );

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            "수정 시도",
            updatedOrderItemRequests
        );

        given(orderService.updateOrder(any(UUID.class), any(UpdateOrderCommand.class)))
            .willThrow(new BusinessException(OrderErrorCode.CANNOT_UPDATE_COMPLETED_ORDER));

        // when & then
        mockMvc.perform(patch("/v1/orders/" + testOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 삭제 - 정상")
    void deleteOrder_Success() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();
        Long deletedBy = 100L;

        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
        ReflectionTestUtils.setField(savedOrder, "orderId", testOrderId);
        savedOrder.delete(deletedBy);

        given(orderService.deleteOrder(any(UUID.class), any(Long.class)))
            .willReturn(savedOrder);

        // when & then
        mockMvc.perform(delete("/v1/orders/" + testOrderId)
                .header("X-User-Id", deletedBy.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("주문이 성공적으로 삭제되었습니다."))
            .andExpect(jsonPath("$.orderId").value(testOrderId.toString()))
            .andExpect(jsonPath("$.deletedBy").value(deletedBy))
            .andExpect(jsonPath("$.deletedAt").exists());
    }

    @Test
    @DisplayName("주문 삭제 - 실패 - 존재하지 않는 주문")
    void deleteOrder_Fail_OrderNotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        Long deletedBy = 100L;

        given(orderService.deleteOrder(any(UUID.class), any(Long.class)))
            .willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(delete("/v1/orders/" + nonExistentId)
                .header("X-User-Id", deletedBy.toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 삭제 - 실패 - deletedBy null")
    void deleteOrder_Fail_DeletedByNull() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/v1/orders/" + testOrderId))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 삭제 - 실패 - deletedBy가 헤더에 없음")
    void deleteOrder_Fail_DeletedByHeaderMissing() throws Exception {
        // given
        UUID testOrderId = UUID.randomUUID();

        given(orderService.deleteOrder(any(UUID.class), any()))
            .willThrow(new BusinessException(OrderErrorCode.DELETED_BY_REQUIRED));

        // when & then
        mockMvc.perform(delete("/v1/orders/" + testOrderId))
            .andExpect(status().isBadRequest());
    }
}
