package com.klp.delivery.delivery.presentation.controller;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_ADDRESS;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_NAME;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_CUSTOMER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_IDEMPOTENCY_KEY;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ITEM_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_SLACK_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_SUPPLIER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createOrderItemDtoList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.DeliveryStatus;
import com.klp.delivery.delivery.application.facade.DeliveryFacade;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.domain.Delivery;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryStatusUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

  @Test
  void 배송생성_성공_201Created() throws Exception {
    // given: 배송 생성 요청 데이터
    DeliveryCreateRequest request = new DeliveryCreateRequest(
        DEFAULT_ORDER_ID,
        DEFAULT_SUPPLIER_ID,
        DEFAULT_CUSTOMER_ID,
        "2025-11-05 14:00까지 납품 요청",
        createOrderItemDtoList(),
        DEFAULT_IDEMPOTENCY_KEY
    );

    List<DeliveryResponse.DeliveryItemResponse> deliveryItems = List.of(
        new DeliveryResponse.DeliveryItemResponse(DEFAULT_ORDER_ITEM_ID, DEFAULT_DELIVERY_ID)
    );
    DeliveryResponse response = new DeliveryResponse(deliveryItems);

    when(deliveryFacade.createDelivery(DEFAULT_ORDER_ID, request)).thenReturn(response);

    // when: 배송 생성 요청
    mockMvc.perform(post("/api/deliveries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.deliveries").isArray())
        .andExpect(jsonPath("$.deliveries[0].orderItemId").value(DEFAULT_ORDER_ITEM_ID.toString()))
        .andExpect(jsonPath("$.deliveries[0].deliveryId").value(DEFAULT_DELIVERY_ID.toString()));

    // then: Facade 호출 검증
    verify(deliveryFacade).createDelivery(DEFAULT_ORDER_ID, request);
  }

  @Test
  void 배송생성_중복멱등키_예외발생() {
    // given: 배송 생성 요청 데이터
    DeliveryCreateRequest request = new DeliveryCreateRequest(
        DEFAULT_ORDER_ID,
        DEFAULT_SUPPLIER_ID,
        DEFAULT_CUSTOMER_ID,
        "2025-11-05 14:00까지 납품 요청",
        createOrderItemDtoList(),
        DEFAULT_IDEMPOTENCY_KEY
    );

    doThrow(new BusinessException(DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY))
        .when(deliveryFacade).createDelivery(DEFAULT_ORDER_ID, request);

    // when & then: 배송 생성 요청 시 예외 발생
    Assertions.assertThrows(Exception.class, () -> {
      mockMvc.perform(post("/api/deliveries")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)));
    });

    // then: Facade 호출 검증
    verify(deliveryFacade).createDelivery(DEFAULT_ORDER_ID, request);
  }

  @Test
  void 배송생성_외부API실패_예외발생() {
    // given: 배송 생성 요청 데이터
    DeliveryCreateRequest request = new DeliveryCreateRequest(
        DEFAULT_ORDER_ID,
        DEFAULT_SUPPLIER_ID,
        DEFAULT_CUSTOMER_ID,
        "2025-11-05 14:00까지 납품 요청",
        createOrderItemDtoList(),
        DEFAULT_IDEMPOTENCY_KEY
    );

    doThrow(new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR))
        .when(deliveryFacade).createDelivery(DEFAULT_ORDER_ID, request);

    // when & then: 배송 생성 요청 시 예외 발생
    Assertions.assertThrows(Exception.class, () -> {
      mockMvc.perform(post("/api/deliveries")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)));
    });

    // then: Facade 호출 검증
    verify(deliveryFacade).createDelivery(DEFAULT_ORDER_ID, request);
  }

  @Test
  void 배송조회_성공_200OK() throws Exception {
    // given: 배송 데이터 준비
    Delivery delivery = createDelivery(DEFAULT_DELIVERY_ID);

    when(deliveryService.getDelivery(DEFAULT_DELIVERY_ID)).thenReturn(delivery);

    // when: 배송 조회 요청
    mockMvc.perform(get("/api/deliveries/{deliveryId}", DEFAULT_DELIVERY_ID))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.deliveryId").value(DEFAULT_DELIVERY_ID.toString()))
        .andExpect(jsonPath("$.orderId").value(DEFAULT_ORDER_ID.toString()))
        .andExpect(jsonPath("$.departureId").value(DEFAULT_DEPARTURE_ID.toString()))
        .andExpect(jsonPath("$.receiverId").value(DEFAULT_RECEIVER_ID.toString()))
        .andExpect(jsonPath("$.receiverName").value(DEFAULT_COMPANY_NAME))
        .andExpect(jsonPath("$.address").value(DEFAULT_COMPANY_ADDRESS))
        .andExpect(jsonPath("$.receiverSlackId").value(DEFAULT_RECEIVER_SLACK_ID))
        .andExpect(jsonPath("$.status").value(DeliveryStatus.CREATED.name()));

    // then: 배송 조회 서비스 호출 검증
    verify(deliveryService).getDelivery(DEFAULT_DELIVERY_ID);
  }

  @Test
  void 배송조회_배송없음_예외발생() {
    // given: 배송이 존재하지 않는 경우
    when(deliveryService.getDelivery(DEFAULT_DELIVERY_ID))
        .thenThrow(new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

    // when & then: 배송 조회 요청 시 예외 발생
    Assertions.assertThrows(Exception.class, () -> {
      mockMvc.perform(get("/api/deliveries/{deliveryId}", DEFAULT_DELIVERY_ID));
    });

    // then: 배송 조회 서비스 호출 검증
    verify(deliveryService).getDelivery(DEFAULT_DELIVERY_ID);
  }

  @Test
  void 배송상태변경_성공_204NoContent() throws Exception {
    // given: 배송 상태 변경 요청 데이터
    DeliveryStatusUpdateRequest request = new DeliveryStatusUpdateRequest(
        DeliveryStatus.AT_HUB_WAITING
    );

    doNothing().when(deliveryService)
        .updateDeliveryStatus(DEFAULT_DELIVERY_ID, DeliveryStatus.AT_HUB_WAITING);

    // when: 배송 상태 변경 요청
    mockMvc.perform(patch("/api/deliveries/{deliveryId}/status", DEFAULT_DELIVERY_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    // then: 배송 상태 변경 서비스 호출 검증
    verify(deliveryService).updateDeliveryStatus(DEFAULT_DELIVERY_ID,
        DeliveryStatus.AT_HUB_WAITING);
  }

  @Test
  void 배송상태변경_status가null_400BadRequest() throws Exception {
    // given: status가 null인 요청 데이터
    String requestJson = "{\"status\": null}";

    // when: 배송 상태 변경 요청
    mockMvc.perform(patch("/api/deliveries/{deliveryId}/status", DEFAULT_DELIVERY_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isBadRequest());

    // then: 배송 상태 변경 서비스가 호출되지 않음
    verify(deliveryService, never()).updateDeliveryStatus(any(), any());
  }

  @ParameterizedTest
  @EnumSource(DeliveryStatus.class)
  void 배송상태변경_모든상태변경_성공(DeliveryStatus status) throws Exception {
    // given: 배송 상태 변경 요청 데이터
    DeliveryStatusUpdateRequest request = new DeliveryStatusUpdateRequest(status);

    doNothing().when(deliveryService).updateDeliveryStatus(DEFAULT_DELIVERY_ID, status);

    // when: 배송 상태 변경 요청
    mockMvc.perform(patch("/api/deliveries/{deliveryId}/status", DEFAULT_DELIVERY_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    // then: 배송 상태 변경 서비스 호출 검증
    verify(deliveryService).updateDeliveryStatus(DEFAULT_DELIVERY_ID, status);
  }
}

