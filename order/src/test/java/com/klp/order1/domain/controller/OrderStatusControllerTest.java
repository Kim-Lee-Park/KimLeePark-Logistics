//package com.klp.order.domain.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.klp.common.exception.BusinessException;
//import com.klp.order.application.command.OrderItemCommand;
//import com.klp.order.application.facade.OrderFacadeV1;
//import com.klp.order.application.service.OrderService;
//import com.klp.order.domain.entity.order.Order;
//import com.klp.order.domain.entity.order.OrderStatus;
//import com.klp.order.global.exception.OrderErrorCode;
//import com.klp.order.presentation.controller.OrderController;
//import com.klp.order.presentation.dto.order.request.update.ChangeOrderStatusRequest;
//import java.util.List;
//import java.util.UUID;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(
//    controllers = OrderController.class,
//    excludeAutoConfiguration = SecurityAutoConfiguration.class)
//@DisplayName("OrderStatusController 테스트")
//public class OrderStatusControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private OrderService orderService;
//
//    @MockitoBean
//    private OrderFacadeV1 orderFacade;
//
//    private Order savedOrder;
//    private List<OrderItemCommand> itemCommands;
//
//    @BeforeEach
//    void setUp() {
//        UUID productId1 = UUID.randomUUID();
//        UUID productId2 = UUID.randomUUID();
//        UUID hubId1 = UUID.randomUUID();
//        UUID hubId2 = UUID.randomUUID();
//
//        itemCommands = List.of(
//            new OrderItemCommand(productId1, "상품명", hubId1, 10),
//            new OrderItemCommand(productId2, "상품명", hubId2, 5)
//        );
//    }
//
//    private Order createOrder(Long supplierId, Long customerId, String comment,
//        List<OrderItemCommand> command) {
//        return Order.create(
//            supplierId,
//            customerId,
//            comment,
//            command
//        );
//    }
//
//    @Test
//    @DisplayName("주문 상태 변경 - 정상")
//    void changeOrderStatus_Success() throws Exception {
//        // given
//        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
//        UUID testOrderId = UUID.randomUUID();
//        ReflectionTestUtils.setField(savedOrder, "orderId", testOrderId);
//
//        savedOrder.changeStatus(OrderStatus.DELIVERY_ASSIGNED);
//
//        ChangeOrderStatusRequest request = new ChangeOrderStatusRequest(
//            OrderStatus.DELIVERY_ASSIGNED
//        );
//
//        given(orderService.changeOrderStatus(any(UUID.class), any(OrderStatus.class)))
//            .willReturn(savedOrder);
//
//        // when & then
//        mockMvc.perform(patch("/v1/orders/{orderId}/status", testOrderId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.orderId").value(testOrderId.toString()))
//            .andExpect(jsonPath("$.orderStatus").value("DELIVERY_ASSIGNED"));
//    }
//
//    @Test
//    @DisplayName("주문 상태 변경 - 실패 - 존재하지 않는 주문")
//    void changeOrderStatus_Fail_OrderNotFound() throws Exception {
//        // given
//        UUID nonExistentId = UUID.randomUUID();
//
//        ChangeOrderStatusRequest request = new ChangeOrderStatusRequest(
//            OrderStatus.DELIVERY_ASSIGNED
//        );
//
//        given(orderService.changeOrderStatus(any(UUID.class), any(OrderStatus.class)))
//            .willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
//
//        // when & then
//        mockMvc.perform(patch("/v1/orders/{orderId}/status", nonExistentId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("주문 상태 변경 - 실패 - orderStatus null")
//    void changeOrderStatus_Fail_OrderStatusNull() throws Exception {
//        // given
//        UUID testOrderId = UUID.randomUUID();
//
//        ChangeOrderStatusRequest invalidRequest = new ChangeOrderStatusRequest(
//            null
//        );
//
//        // when & then
//        mockMvc.perform(patch("/v1/orders/{orderId}/status", testOrderId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(invalidRequest)))
//            .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("주문 상태 변경 - 실패 - 취소된 주문")
//    void changeOrderStatus_Fail_CancelledOrder() throws Exception {
//        // given
//        UUID testOrderId = UUID.randomUUID();
//
//        ChangeOrderStatusRequest request = new ChangeOrderStatusRequest(
//            OrderStatus.DELIVERY_ASSIGNED
//        );
//
//        given(orderService.changeOrderStatus(any(UUID.class), any(OrderStatus.class)))
//            .willThrow(new BusinessException(OrderErrorCode.CANNOT_CHANGE_CANCELLED_ORDER_STATUS));
//
//        // when & then
//        mockMvc.perform(patch("/v1/orders/{orderId}/status", testOrderId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("주문 상태 변경 - ING에서 DELIVERY_ASSIGNED로 변경")
//    void changeOrderStatus_ING_to_DELIVERY_ASSIGNED() throws Exception {
//        // given
//        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
//        UUID testOrderId = UUID.randomUUID();
//        ReflectionTestUtils.setField(savedOrder, "orderId", testOrderId);
//
//        savedOrder.changeStatus(OrderStatus.DELIVERY_ASSIGNED);
//
//        ChangeOrderStatusRequest request = new ChangeOrderStatusRequest(
//            OrderStatus.DELIVERY_ASSIGNED
//        );
//
//        given(orderService.changeOrderStatus(any(UUID.class), any(OrderStatus.class)))
//            .willReturn(savedOrder);
//
//        // when & then
//        mockMvc.perform(patch("/v1/orders/{orderId}/status", testOrderId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.orderId").value(testOrderId.toString()))
//            .andExpect(jsonPath("$.orderStatus").value("DELIVERY_ASSIGNED"));
//    }
//
//    @Test
//    @DisplayName("주문 상태 변경 - DELIVERY_ASSIGNED에서 COMPLETE로 변경")
//    void changeOrderStatus_DELIVERY_ASSIGNED_to_COMPLETE() throws Exception {
//        // given
//        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
//        UUID testOrderId = UUID.randomUUID();
//        ReflectionTestUtils.setField(savedOrder, "orderId", testOrderId);
//
//        savedOrder.changeStatus(OrderStatus.DELIVERY_ASSIGNED);
//        savedOrder.changeStatus(OrderStatus.COMPLETE);
//
//        ChangeOrderStatusRequest request = new ChangeOrderStatusRequest(
//            OrderStatus.COMPLETE
//        );
//
//        given(orderService.changeOrderStatus(any(UUID.class), any(OrderStatus.class)))
//            .willReturn(savedOrder);
//
//        // when & then
//        mockMvc.perform(patch("/v1/orders/{orderId}/status", testOrderId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.orderId").value(testOrderId.toString()))
//            .andExpect(jsonPath("$.orderStatus").value("COMPLETE"));
//    }
//
//    @Test
//    @DisplayName("주문 상태 변경 - 배송 실패로 변경")
//    void changeOrderStatus_to_FAILED() throws Exception {
//        // given
//        savedOrder = createOrder(1L, 2L, "요구사항", itemCommands);
//        UUID testOrderId = UUID.randomUUID();
//        ReflectionTestUtils.setField(savedOrder, "orderId", testOrderId);
//
//        savedOrder.changeStatus(OrderStatus.DELIVERY_ASSIGNED);
//        savedOrder.changeStatus(OrderStatus.FAILED);
//
//        ChangeOrderStatusRequest request = new ChangeOrderStatusRequest(
//            OrderStatus.FAILED
//        );
//
//        given(orderService.changeOrderStatus(any(UUID.class), any(OrderStatus.class)))
//            .willReturn(savedOrder);
//
//        // when & then
//        mockMvc.perform(patch("/v1/orders/{orderId}/status", testOrderId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.orderId").value(testOrderId.toString()))
//            .andExpect(jsonPath("$.orderStatus").value("FAILED"));
//    }
//}
