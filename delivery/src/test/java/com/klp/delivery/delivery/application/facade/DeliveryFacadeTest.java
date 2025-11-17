package com.klp.delivery.delivery.application.facade;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_CUSTOMER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_FIRST;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_SECOND;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_HUB_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createCompany;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryRequest;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDriver;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.DEFAULT_HUB_ID_UUID_FIRST;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.DEFAULT_HUB_ID_UUID_SECOND;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_FIRST;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItemListWithDeliveryId;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItems;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import java.util.List;
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

        Delivery delivery = createDelivery(DEFAULT_DELIVERY_ID_FIRST);

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString())).thenReturn(createCompany());
        when(deliveryService.findDriver(DEFAULT_HUB_ID)).thenReturn(createDriver());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class), anyList())).thenReturn(delivery);
        doNothing().when(idempotencyKeyService).updateIdempotencyStatus(any(IdempotencyCommand.class));

        // when: 배송 생성
        DeliveryResponse result = deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        // then: 생성 검증
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).orderItemId()).isEqualTo(ORDER_ITEM_ID_FIRST);
        assertThat(result.items().get(0).deliveryId()).isEqualTo(DEFAULT_DELIVERY_ID_FIRST);

        // then: 멱등키 등록, 업체/담당자 조회, 배송 생성, 멱등키 업데이트 검증
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(any(IdempotencyCommand.class));
        verify(deliveryService, times(1)).findCompany(DEFAULT_CUSTOMER_ID.toString());
        verify(deliveryService, times(1)).findDriver(DEFAULT_HUB_ID);
        verify(deliveryService, times(1)).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, times(1)).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }

    @Test
    void 배송생성_성공_여러아이템_다른출발지() {

        // given: 여러 아이템을 가진 배송 생성 요청 데이터 준비
        // orderItem3개 / hubId 2개
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItems());

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString())).thenReturn(createCompany());
        when(deliveryService.findDriver(DEFAULT_HUB_ID)).thenReturn(createDriver());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class), anyList()))
            .thenAnswer(invocation -> {
                DeliveryCommand cmd = invocation.getArgument(0);
                @SuppressWarnings("unchecked")
                List<OrderItemCommand> orderItems = invocation.getArgument(1);

                // 실제 전달된 orderItems로 Delivery 생성
                Delivery delivery = Delivery.create(
                    cmd.vendorDriverId(),
                    cmd.orderId(),
                    cmd.departureId(),
                    cmd.arrivalId(),
                    cmd.senderId(),
                    cmd.receiverId(),
                    cmd.receiverName(),
                    cmd.address(),
                    cmd.receiverSlackId(),
                    orderItems
                );

                // deliveryId 설정
                try {
                    var field = Delivery.class.getDeclaredField("deliveryId");
                    field.setAccessible(true);
                    if (cmd.departureId().equals(DEFAULT_HUB_ID_UUID_FIRST)) {
                        field.set(delivery, DEFAULT_DELIVERY_ID_FIRST);
                    } else if (cmd.departureId().equals(DEFAULT_HUB_ID_UUID_SECOND)) {
                        field.set(delivery, DEFAULT_DELIVERY_ID_SECOND);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return delivery;
            });
        doNothing().when(idempotencyKeyService).updateIdempotencyStatus(any(IdempotencyCommand.class));

        // when: 배송 생성
        DeliveryResponse result = deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        // then: 생성 검증
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(3);

        // hubId1 그룹: 2개 아이템 (ORDER_ITEM_ID_FIRST, ORDER_ITEM_ID_SECOND)
        assertThat(result.items().get(0).deliveryId()).isEqualTo(DEFAULT_DELIVERY_ID_FIRST);
        assertThat(result.items().get(1).deliveryId()).isEqualTo(DEFAULT_DELIVERY_ID_FIRST);

        // hubId2 그룹: 1개 아이템 (ORDER_ITEM_ID_THIRD)
        assertThat(result.items().get(2).deliveryId()).isEqualTo(DEFAULT_DELIVERY_ID_SECOND);

        // then: 각 아이템에 대해 다른 departureId로 배송 생성되었는지 검증
        verify(deliveryService, times(2)).registerDelivery(any(DeliveryCommand.class), anyList());

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
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }

    @Test
    void 배송생성_멱등키_상태_업데이트_검증() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doThrow(new BusinessException(DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY))
            .when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));



        // then: 멱등키 등록만 호출되고, 업체/담당자 조회 및 배송 생성은 호출되지 않음
        verify(idempotencyKeyService, times(1)).updateIdempotencyStatus(any(IdempotencyCommand.class));
        verify(deliveryService, never()).findCompany(any());
        verify(deliveryService, never()).findDriver(any());
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }


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
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class), anyList());
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
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }

    @Test
    void 배송생성_실패_배송저장실패() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doNothing().when(idempotencyKeyService).registerIdempotencyKey(any(IdempotencyCommand.class));
        when(deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString())).thenReturn(createCompany());
        when(deliveryService.findDriver(DEFAULT_HUB_ID)).thenReturn(createDriver());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class), anyList()))
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
        verify(deliveryService, times(1)).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(any(IdempotencyCommand.class));
    }


}
