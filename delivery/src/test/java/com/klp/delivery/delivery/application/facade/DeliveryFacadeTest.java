package com.klp.delivery.delivery.application.facade;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_FIRST;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_SECOND;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_USER_ADDRESS_HUB_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createArrivalHubInfo;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryFromCommand;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryRequest;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryWithItems;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDepartureHubInfo;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDrivers;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.DEFAULT_HUB_ID_UUID_SECOND;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_FIRST;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItemListWithDeliveryId;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItems;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.DriverService;
import com.klp.delivery.delivery.application.service.HubService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.event.DeliveryRouteCreateEvent;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import com.klp.delivery.global.exception.BusinessException;
import com.klp.delivery.routeplan.application.service.HubClientService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

@Disabled
public class DeliveryFacadeTest extends MockTest {

    @InjectMocks
    DeliveryFacade deliveryFacade;

    @Mock
    DeliveryService deliveryService;

    @Mock
    IdempotencyKeyService idempotencyKeyService;

    @Mock
    DriverService driverService;

    @Mock
    HubClientService hubClientService;

    @Mock
    HubService hubService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void 배송생성_성공_단일아이템() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        // 실제 요청의 OrderItem 수에 맞는 Delivery 생성 (1개)
        List<OrderItemCommand> orderItemCommands = request.toOrderToDeliveryCommand().products();
        Delivery delivery = createDeliveryWithItems(DEFAULT_DELIVERY_ID_FIRST, orderItemCommands);

        doNothing().when(idempotencyKeyService)
            .registerIdempotencyKey(any(IdempotencyCommand.class));
        when(hubClientService.getHubById(DEFAULT_USER_ADDRESS_HUB_ID)).thenReturn(
            createArrivalHubInfo());
        when(hubClientService.getHubById(DEFAULT_DEPARTURE_ID)).thenReturn(
            createDepartureHubInfo());
        when(driverService.findArrivalHubDrivers(any(UUID.class))).thenReturn(createDrivers());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class), anyList())).thenReturn(
            delivery);
        doNothing().when(idempotencyKeyService)
            .updateIdempotencyStatus(any(IdempotencyCommand.class));
        doNothing().when(eventPublisher).publishEvent(any(DeliveryRouteCreateEvent.class));

        // when: 배송 생성
        DeliveryResponse result = deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand());

        // then: 생성 검증
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).orderItemId()).isEqualTo(ORDER_ITEM_ID_FIRST);
        assertThat(result.items().get(0).deliveryId()).isEqualTo(DEFAULT_DELIVERY_ID_FIRST);

        // then: 멱등키 등록, 업체/담당자 조회, 배송 생성, 멱등키 업데이트 검증
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(
            any(IdempotencyCommand.class));
        // arrival hub 조회: userAddressHubId (DEFAULT_ARRIVAL_ID) 1번
        verify(hubClientService, times(1)).getHubById(DEFAULT_ARRIVAL_ID);
        // departure hub 조회: 아이템의 hubId (DEFAULT_DEPARTURE_ID) 1번
        verify(hubClientService, times(1)).getHubById(DEFAULT_DEPARTURE_ID);
        verify(driverService, times(1)).findArrivalHubDrivers(any(UUID.class));
        verify(deliveryService, times(1)).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, times(1)).updateIdempotencyStatus(
            any(IdempotencyCommand.class));
        verify(eventPublisher, times(1)).publishEvent(any(DeliveryRouteCreateEvent.class));
    }

    @Test
    void 배송생성_성공_여러아이템_다른출발지() {

        // given: 여러 아이템을 가진 배송 생성 요청 데이터 준비
        // orderItem3개 / hubId 2개
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItems());

        doNothing().when(idempotencyKeyService)
            .registerIdempotencyKey(any(IdempotencyCommand.class));
//        when(hubService.findHubInfo(any()))
//            .thenAnswer(invocation -> {
//                String hubIdStr = invocation.getArgument(0);
//                UUID hubId = UUID.fromString(hubIdStr);
//
//                if (hubId.equals(DEFAULT_DEPARTURE_ID)) {
//                    return createDeparutreInfoCommand();
//                }
//                if (hubId.equals(DEFAULT_ARRIVAL_ID)) {
//                    return createArrivalInfoCommand();
//                }
//
//                return null;
//            });
        when(hubClientService.getHubById(DEFAULT_USER_ADDRESS_HUB_ID)).thenReturn(
            createArrivalHubInfo());
        when(hubClientService.getHubById(DEFAULT_DEPARTURE_ID)).thenReturn(
            createDepartureHubInfo());
        when(hubClientService.getHubById(DEFAULT_HUB_ID_UUID_SECOND)).thenReturn(
            new com.klp.delivery.routeplan.application.command.HubInfo(
                DEFAULT_HUB_ID_UUID_SECOND,
                "다른출발센터",
                com.klp.delivery.delivery.fixture.DeliveryFixture.DEFALT_HUB_LATITUDE,
                com.klp.delivery.delivery.fixture.DeliveryFixture.DEFALT_HUB_LONGITUDE,
                com.klp.delivery.delivery.fixture.DeliveryFixture.DEFALT_HUB_ADDRESS,
                com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_HUB_STATUS
            ));
        when(driverService.findArrivalHubDrivers(any(UUID.class))).thenReturn(createDrivers());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class), anyList()))
            .thenAnswer(invocation -> createDeliveryFromCommand(
                invocation.getArgument(0),
                invocation.getArgument(1)
            ));
        doNothing().when(idempotencyKeyService)
            .updateIdempotencyStatus(any(IdempotencyCommand.class));
        doNothing().when(eventPublisher).publishEvent(any(DeliveryRouteCreateEvent.class));

        // when: 배송 생성
        DeliveryResponse result = deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand());

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
        assertThatThrownBy(() -> deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand()))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY);
            });

        // then: 멱등키 등록만 호출되고, 업체/담당자 조회 및 배송 생성은 호출되지 않음
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(
            any(IdempotencyCommand.class));
        verify(hubClientService, never()).getHubById(any());
        verify(driverService, never()).findArrivalHubDrivers(any());
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(
            any(IdempotencyCommand.class));
    }

    @Test
    void 배송생성_멱등키_상태_업데이트_검증() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        Delivery delivery = createDelivery(DEFAULT_DELIVERY_ID_FIRST);

        // 멱등키 등록 수정 정상적으로 수행 된다고 가정
        doNothing().when(idempotencyKeyService)
            .registerIdempotencyKey(any(IdempotencyCommand.class));
        when(hubClientService.getHubById(DEFAULT_USER_ADDRESS_HUB_ID)).thenReturn(
            createArrivalHubInfo());
        when(hubClientService.getHubById(DEFAULT_DEPARTURE_ID)).thenReturn(
            createDepartureHubInfo());
        when(driverService.findArrivalHubDrivers(any(UUID.class))).thenReturn(createDrivers());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class), anyList())).thenReturn(
            delivery);
        doNothing().when(idempotencyKeyService)
            .updateIdempotencyStatus(any(IdempotencyCommand.class));
        doNothing().when(eventPublisher).publishEvent(any(DeliveryRouteCreateEvent.class));

        // when: 배송 생성
        deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand());

        // then: 멱등키 상태 업데이트가 호출되었는지 검증
        verify(idempotencyKeyService, times(1)).updateIdempotencyStatus(
            any(IdempotencyCommand.class));
    }


    @Test
    void 배송생성_실패_업체조회실패() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doNothing().when(idempotencyKeyService)
            .registerIdempotencyKey(any(IdempotencyCommand.class));
        when(hubClientService.getHubById(DEFAULT_USER_ADDRESS_HUB_ID))
            .thenThrow(new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR));
        doNothing().when(idempotencyKeyService)
            .deleteIdempotencyKey(anyString());

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand()))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.DELIVERY_CREATION_FAILED);
            });

        // then: 멱등키 등록 및 허브 조회는 호출되지만, 담당자 조회 및 배송 생성은 호출되지 않음
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(
            any(IdempotencyCommand.class));
        verify(hubClientService, times(1)).getHubById(DEFAULT_USER_ADDRESS_HUB_ID);
        verify(driverService, never()).findArrivalHubDrivers(any());
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(
            any(IdempotencyCommand.class));
        // 보상 트랜잭션: 멱등키 삭제 호출 검증
        verify(idempotencyKeyService, times(1)).deleteIdempotencyKey(anyString());
    }

    @Test
    void 배송생성_실패_담당자조회실패() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doNothing().when(idempotencyKeyService)
            .registerIdempotencyKey(any(IdempotencyCommand.class));
        // when & then: 예외 발생 검증
        when(hubClientService.getHubById(DEFAULT_USER_ADDRESS_HUB_ID)).thenReturn(
            createArrivalHubInfo());
        when(driverService.findArrivalHubDrivers(any(UUID.class))).thenThrow(
            new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR));
        doNothing().when(idempotencyKeyService)
            .deleteIdempotencyKey(anyString());

        assertThatThrownBy(() ->
            deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
                request.toIdempotencyCommand())
        )
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.DELIVERY_CREATION_FAILED);
            });

        // then: 멱등키 등록, 허브 조회, 담당자 조회는 호출되지만, 배송 생성은 호출되지 않음
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(
            any(IdempotencyCommand.class));
        verify(hubClientService, times(1)).getHubById(DEFAULT_USER_ADDRESS_HUB_ID);
        verify(driverService, times(1)).findArrivalHubDrivers(any(UUID.class));
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(
            any(IdempotencyCommand.class));
        // 보상 트랜잭션: 멱등키 삭제 호출 검증
        verify(idempotencyKeyService, times(1)).deleteIdempotencyKey(anyString());
    }

    @Test
    void 배송생성_실패_배송저장실패() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());

        doNothing().when(idempotencyKeyService)
            .registerIdempotencyKey(any(IdempotencyCommand.class));
        when(hubClientService.getHubById(DEFAULT_ARRIVAL_ID)).thenReturn(createArrivalHubInfo());
        when(hubClientService.getHubById(DEFAULT_DEPARTURE_ID)).thenReturn(
            createDepartureHubInfo());
        when(driverService.findArrivalHubDrivers(any(UUID.class))).thenReturn(createDrivers());
        when(deliveryService.registerDelivery(any(DeliveryCommand.class), anyList()))
            .thenThrow(new BusinessException(DeliveryErrorCode.DELIVERY_CREATION_FAILED));
        doNothing().when(idempotencyKeyService)
            .deleteIdempotencyKey(anyString());

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand()))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.DELIVERY_CREATION_FAILED);
            });

        // then: 멱등키 등록, 허브/담당자 조회, 배송 생성은 호출되지만, 멱등키 업데이트는 호출되지 않음
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(
            any(IdempotencyCommand.class));
        verify(hubClientService, times(1)).getHubById(DEFAULT_ARRIVAL_ID);
        verify(hubClientService, times(1)).getHubById(DEFAULT_DEPARTURE_ID);
        verify(driverService, times(1)).findArrivalHubDrivers(any(UUID.class));
        verify(deliveryService, times(1)).registerDelivery(any(DeliveryCommand.class), anyList());
        verify(idempotencyKeyService, never()).updateIdempotencyStatus(
            any(IdempotencyCommand.class));
        // 보상 트랜잭션: 멱등키 삭제 호출 검증
        verify(idempotencyKeyService, times(1)).deleteIdempotencyKey(anyString());
    }

    @Test
    void 배송생성_실패_보상트랜잭션_멱등키삭제_성공() {
        // given: 배송 생성 요청 데이터 준비
        DeliveryCreateRequest request = createDeliveryRequest(createOrderItemListWithDeliveryId());
        String idempotencyKey = request.toIdempotencyCommand().idempotencyKey();

        doNothing().when(idempotencyKeyService)
            .registerIdempotencyKey(any(IdempotencyCommand.class));
        when(hubClientService.getHubById(DEFAULT_USER_ADDRESS_HUB_ID))
            .thenThrow(new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR));
        doNothing().when(idempotencyKeyService)
            .deleteIdempotencyKey(idempotencyKey);

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> deliveryFacade.createDelivery(request.toOrderToDeliveryCommand(),
            request.toIdempotencyCommand()))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.DELIVERY_CREATION_FAILED);
            });

        // then: 보상 트랜잭션 - 멱등키 삭제가 성공적으로 호출됨
        verify(idempotencyKeyService, times(1)).registerIdempotencyKey(
            any(IdempotencyCommand.class));
        verify(hubClientService, times(1)).getHubById(DEFAULT_USER_ADDRESS_HUB_ID);
        verify(idempotencyKeyService, times(1)).deleteIdempotencyKey(idempotencyKey);
        verify(deliveryService, never()).registerDelivery(any(DeliveryCommand.class), anyList());
    }


}
