package com.klp.delivery.delivery.application.event;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_FIRST;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_VENDOR_DRIVER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DeliveryRouteCommand;
import com.klp.delivery.delivery.application.facade.DeliveryRouteFacade;
import com.klp.delivery.delivery.domain.event.DeliveryRouteCreateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("배송 경로 생성 이벤트 리스너 테스트")
class DeliveryRouteCreateEventListenerTest extends MockTest {

    @InjectMocks
    DeliveryRouteCreateEventListener listener;

    @Mock
    DeliveryRouteFacade deliveryRouteFacade;

    @Test
    @DisplayName("배송 경로 생성 이벤트 처리 성공")
    void handleDeliveryRouteCreateEvent_성공() {
        // given
        DeliveryRouteCreateEvent event = new DeliveryRouteCreateEvent(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_VENDOR_DRIVER_ID
        );

        // when
        listener.handleDeliveryRouteCreateEvent(event);

        // then: DeliveryRouteFacade가 올바른 Command로 호출되었는지 검증
        verify(deliveryRouteFacade, times(1)).CreateDeliveryRoute(any(DeliveryRouteCommand.class));
    }

    @Test
    @DisplayName("이벤트 파라미터가 올바르게 Command로 변환되는지 검증")
    void handleDeliveryRouteCreateEvent_파라미터_변환_검증() {
        // given
        DeliveryRouteCreateEvent event = new DeliveryRouteCreateEvent(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_VENDOR_DRIVER_ID
        );

        // when
        listener.handleDeliveryRouteCreateEvent(event);

        // then: DeliveryRouteCommand가 올바른 파라미터로 생성되었는지 검증
        verify(deliveryRouteFacade).CreateDeliveryRoute(any(DeliveryRouteCommand.class));
    }
}

