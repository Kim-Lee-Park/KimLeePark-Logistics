package com.klp.delivery.delivery.presentation.controller;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_ADDRESS;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_NAME;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_FIRST;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_SLACK_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryRequest;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.DEFAULT_HUB_ID_UUID_FIRST;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.DEFAULT_HUB_ID_UUID_SECOND;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_FIRST;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_SECOND;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_THIRD;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItems;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.application.facade.DeliveryFacade;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DeliveryController.class)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeliveryFacade deliveryFacade;

    @MockitoBean
    private DeliveryService deliveryService;

    @MockitoBean
    private IdempotencyKeyService idempotencyKeyService;

    @Test
    void 배송생성_성공_200Created() throws Exception {

        // given: 여러 아이템을 가진 배송 생성 요청 데이터 준비
        // orderItem3개 / hubId 2개
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItems());

        List<DeliveryResponse.DeliveryItemResponse> deliveryItems = List.of(
            new DeliveryResponse.DeliveryItemResponse(ORDER_ITEM_ID_FIRST,
                DEFAULT_HUB_ID_UUID_FIRST),
            new DeliveryResponse.DeliveryItemResponse(ORDER_ITEM_ID_SECOND,
                DEFAULT_HUB_ID_UUID_FIRST),
            new DeliveryResponse.DeliveryItemResponse(ORDER_ITEM_ID_THIRD,
                DEFAULT_HUB_ID_UUID_SECOND)
        );

        DeliveryResponse response = new DeliveryResponse(DEFAULT_ORDER_ID, deliveryItems);

        when(deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand())).thenReturn(response);

        // when: 배송 생성 요청
        mockMvc.perform(post("/v1/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.items").isArray());

        // then: Facade 호출 검증
        verify(deliveryFacade).createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand());
    }

    @Test
    void 배송생성_중복멱등키_예외발생() {
        // given: 배송 생성 요청 데이터
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItems());

        doThrow(new BusinessException(DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY))
            .when(deliveryFacade)
            .createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        // when & then: 배송 생성 요청 시 예외 발생
        Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/v1/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        });

        // then: Facade 호출 검증
        verify(deliveryFacade).createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand());
    }

    @Test
    void 배송생성_외부API실패_예외발생() {
        // given: 배송 생성 요청 데이터
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItems());

        doThrow(new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR))
            .when(deliveryFacade)
            .createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        // when & then: 배송 생성 요청 시 예외 발생
        Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/v1/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        });

        // then: Facade 호출 검증
        verify(deliveryFacade).createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand());
    }

    @Test
    void 배송조회_성공_200OK() throws Exception {
        // given: 배송 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItems());

        List<DeliveryResponse.DeliveryItemResponse> deliveryItems = List.of(
            new DeliveryResponse.DeliveryItemResponse(ORDER_ITEM_ID_FIRST,
                DEFAULT_HUB_ID_UUID_FIRST),
            new DeliveryResponse.DeliveryItemResponse(ORDER_ITEM_ID_SECOND,
                DEFAULT_HUB_ID_UUID_FIRST),
            new DeliveryResponse.DeliveryItemResponse(ORDER_ITEM_ID_THIRD,
                DEFAULT_HUB_ID_UUID_SECOND)
        );

        Delivery delivery = createDelivery(DEFAULT_DELIVERY_ID_FIRST);

        when(deliveryService.findDelivery(DEFAULT_DELIVERY_ID_FIRST)).thenReturn(delivery);

        // when: 배송 조회 요청
        mockMvc.perform(get("/v1/deliveries/{deliveryId}", delivery.getDeliveryId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.deliveryId").value(DEFAULT_DELIVERY_ID_FIRST.toString()))
            .andExpect(jsonPath("$.orderId").value(DEFAULT_ORDER_ID.toString()))
            .andExpect(jsonPath("$.departureId").value(DEFAULT_DEPARTURE_ID.toString()))
            .andExpect(jsonPath("$.receiverId").value(DEFAULT_RECEIVER_ID.toString()))
            .andExpect(jsonPath("$.receiverName").value(DEFAULT_COMPANY_NAME))
            .andExpect(jsonPath("$.address").value(DEFAULT_COMPANY_ADDRESS))
            .andExpect(jsonPath("$.receiverSlackId").value(DEFAULT_RECEIVER_SLACK_ID))
            .andExpect(jsonPath("$.status").value(DeliveryStatus.CREATED.name()));

        // then: 배송 조회 서비스 호출 검증
        verify(deliveryService).findDelivery(DEFAULT_DELIVERY_ID_FIRST);
    }

    @Test
    void 배송조회_배송없음_예외발생() {
        // given: 배송이 존재하지 않는 경우
        when(deliveryService.findDelivery(DEFAULT_DELIVERY_ID_FIRST))
            .thenThrow(new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        // when & then: 배송 조회 요청 시 예외 발생
        Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/v1/deliveries/{deliveryId}", DEFAULT_DELIVERY_ID_FIRST));
        });

        // then: 배송 조회 서비스 호출 검증
        verify(deliveryService).findDelivery(DEFAULT_DELIVERY_ID_FIRST);
    }

}

