package com.klp.delivery.delivery.application.facade;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_CUSTOMER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_HUB_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ITEM_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createCompany;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryRequest;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDriver;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.DEFAULT_HUB_ID_UUID;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItem;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItemListWithDeliveryId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest.OrderItem;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class DeliveryFacadeTest extends MockTest {

  @InjectMocks
  DeliveryFacade deliveryFacade;

  @Mock
  DeliveryService deliveryService;

  @Mock
  IdempotencyKeyService idempotencyKeyService;

    @Test
    void 배송생성_성공_단일아이템() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        Delivery delivery = createDelivery(DEFAULT_DELIVERY_ID);

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString())).thenReturn(createCompany());
        when(deliveryService.findDriver(DEFAULT_HUB_ID)).thenReturn(createDriver());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class))).thenReturn(delivery);
        doNothing().when(idempotencyKeyService).updateIdempotencyStatus(any(IdempotencyCommand.class));

        // when: 배송 생성
        DeliveryResponse result = deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        // then: 생성 검증
        assertThat(result).isNotNull();
        assertThat(result.deliveries()).hasSize(1);
        assertThat(result.deliveries().get(0).orderItemId()).isEqualTo(DEFAULT_ORDER_ITEM_ID);
        assertThat(result.deliveries().get(0).deliveryId()).isEqualTo(DEFAULT_DELIVERY_ID);

        // then: 멱등키 등록, 업체/담당자 조회, 배송 생성, 멱등키 업데이트 검증
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(any(IdempotencyCommand.class));
        verify(deliveryService, times(1)).findCompany(DEFAULT_CUSTOMER_ID.toString());
        verify(deliveryService, times(1)).findDriver(DEFAULT_HUB_ID);
        verify(deliveryService, times(1)).registerDelivery(any(DeliveryCommand.class));
        verify(idempotencyKeyService, times(1)).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }

    @Test
    void 배송생성_성공_여러아이템_다른출발지() {

        // given: 여러 아이템을 가진 배송 생성 요청 데이터 준비
        UUID orderItemId1 = DEFAULT_ORDER_ITEM_ID;
        UUID orderItemId2 = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UUID hubId1 = DEFAULT_HUB_ID_UUID;
        UUID hubId2 = UUID.fromString("00000000-0000-0000-0000-000000000011");

        OrderItem orderItem1 = createOrderItem(orderItemId1, hubId1);
        OrderItem orderItem2 = createOrderItem(orderItemId2, hubId2);
        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

        DeliveryCreateRequest request = createDeliveryRequest(orderItems);

        // 기본 배송 객체 생성 후, 리플렉션으로 deliveryId를 지정
        Delivery delivery1 = createDelivery(DEFAULT_DELIVERY_ID);
        UUID deliveryId2 = UUID.fromString("00000000-0000-0000-0000-000000000013");
        Delivery delivery2 = createDelivery(deliveryId2);

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString())).thenReturn(createCompany());
        when(deliveryService.findDriver(DEFAULT_HUB_ID)).thenReturn(createDriver());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class)))
            .thenAnswer(invocation -> {
                DeliveryCommand cmd = invocation.getArgument(0);
                if (cmd.departureId().equals(hubId1)) {
                    return delivery1;
                } else if (cmd.departureId().equals(hubId2)) {
                    return delivery2;
                }
                return delivery1;
            });
        doNothing().when(idempotencyKeyService).updateIdempotencyStatus(any(IdempotencyCommand.class));

        // when: 배송 생성
        DeliveryResponse result = deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        // then: 생성 검증
        assertThat(result).isNotNull();
        assertThat(result.deliveries()).hasSize(2);
        assertThat(result.deliveries().get(0).orderItemId()).isEqualTo(orderItemId1);
        assertThat(result.deliveries().get(0).deliveryId()).isEqualTo(DEFAULT_DELIVERY_ID);
        assertThat(result.deliveries().get(1).orderItemId()).isEqualTo(orderItemId2);
        assertThat(result.deliveries().get(1).deliveryId()).isEqualTo(deliveryId2);

        // then: 각 아이템에 대해 다른 departureId로 배송 생성되었는지 검증
        verify(deliveryService, times(2)).registerDelivery(any(DeliveryCommand.class));
        verify(deliveryService).registerDelivery(argThat(cmd ->
            cmd.departureId().equals(hubId1) && cmd.arrivalId()
                .equals(UUID.fromString(createCompany().hubId()))
        ));
        verify(deliveryService).registerDelivery(argThat(cmd ->
            cmd.departureId().equals(hubId2) && cmd.arrivalId()
                .equals(UUID.fromString(createCompany().hubId()))
        ));
    }


    @Test
    void 배송생성_실패_멱등키중복() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doThrow(new BusinessException(DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY))
            .when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand()))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY);
            });

        // then: 멱등키 등록만 호출되고, 업체/담당자 조회 및 배송 생성은 호출되지 않음
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(any(IdempotencyCommand.class));
        verify(deliveryService, never()).findCompany(any());
        verify(deliveryService, never()).findDriver(any());
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class));
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }
//
//
    @Test
    void 배송생성_실패_업체조회실패() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString()))
            .thenThrow(new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR));

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand()))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.EXTERNAL_API_ERROR);
            });

        // then: 멱등키 등록 및 업체 조회는 호출되지만, 담당자 조회 및 배송 생성은 호출되지 않음
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(any(IdempotencyCommand.class));
        verify(deliveryService, times(1)).findCompany(DEFAULT_CUSTOMER_ID.toString());
        verify(deliveryService, never()).findDriver(any());
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class));
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }

    @Test
    void 배송생성_실패_담당자조회실패() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        // when & then: 예외 발생 검증
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString())).thenReturn(createCompany());
        when(deliveryService.findDriver(DEFAULT_HUB_ID)).thenThrow(new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR));
        assertThatThrownBy(() ->
            deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand())
        )
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.EXTERNAL_API_ERROR);
            });

        // then: 멱등키 등록, 업체 조회, 담당자 조회는 호출되지만, 배송 생성은 호출되지 않음
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(any(IdempotencyCommand.class));
        verify(deliveryService, times(1)).findCompany(DEFAULT_CUSTOMER_ID.toString());
        verify(deliveryService, times(1)).findDriver(DEFAULT_HUB_ID.toString());
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class));
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }

    @Test
    void 배송생성_실패_배송저장실패() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString())).thenReturn(createCompany());
        when(deliveryService.findDriver(DEFAULT_HUB_ID)).thenReturn(createDriver());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class)))
            .thenThrow(new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR));

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand()))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.EXTERNAL_API_ERROR);
            });

        // then: 멱등키 등록, 업체/담당자 조회, 배송 생성은 호출되지만, 멱등키 업데이트는 호출되지 않음
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(any(IdempotencyCommand.class));
        verify(deliveryService, times(1)).findCompany(DEFAULT_CUSTOMER_ID.toString());
        verify(deliveryService, times(1)).findDriver(DEFAULT_HUB_ID.toString());
        verify(deliveryService, times(1)).registerDelivery(any(DeliveryCommand.class));
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }

    @Test
    void 배송생성_성공_모든아이템의도착지가같음() {

        // given: 여러 아이템을 가진 배송 생성 요청 데이터 준비
        UUID orderItemId2 = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UUID hubId2 = UUID.fromString("00000000-0000-0000-0000-000000000011");

        OrderItem orderItem1 = createOrderItem();
        OrderItem orderItem2 = createOrderItem(orderItemId2, hubId2);
        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

        DeliveryCreateRequest request = createDeliveryRequest(orderItems);

        Delivery delivery1 = createDelivery(DEFAULT_DELIVERY_ID);
        UUID deliveryId2 = UUID.fromString("00000000-0000-0000-0000-000000000013");
        Delivery delivery2 = createDelivery(deliveryId2);

        UUID expectedArrivalId = UUID.fromString(createCompany().hubId());

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString())).thenReturn(createCompany());
        when(deliveryService.findDriver(DEFAULT_HUB_ID)).thenReturn(createDriver());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class)))
            .thenAnswer(invocation -> {
                DeliveryCommand cmd = invocation.getArgument(0);
                if (cmd.departureId().equals(DEFAULT_HUB_ID_UUID)) {
                    return delivery1;
                } else if (cmd.departureId().equals(hubId2)) {
                    return delivery2;
                }
                return delivery1;
            });
        doNothing().when(idempotencyKeyService).updateIdempotencyStatus(any(IdempotencyCommand.class));

        // when: 배송 생성
        DeliveryResponse result = deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        // then: 생성 검증
        assertThat(result).isNotNull();
        assertThat(result.deliveries()).hasSize(2);

        // then: 모든 아이템의 arrivalId가 업체의 hubId와 같은지 검증
        verify(deliveryService, times(2)).registerDelivery(argThat(cmd ->
            cmd.arrivalId().equals(expectedArrivalId)
        ));
        verify(deliveryService).registerDelivery(argThat(cmd ->
            cmd.departureId().equals(DEFAULT_HUB_ID_UUID) && cmd.arrivalId().equals(expectedArrivalId)
        ));
        verify(deliveryService).registerDelivery(argThat(cmd ->
            cmd.departureId().equals(hubId2) && cmd.arrivalId().equals(expectedArrivalId)
        ));
    }

}
