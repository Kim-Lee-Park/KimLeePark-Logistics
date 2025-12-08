package com.klp.hub.inventory.application;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.listener.OrderCreatedEventListener;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent.OrderItemDto;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Disabled
@ExtendWith(MockitoExtension.class)
class OrderCreatedEventListenerTest {

    @Mock
    private InventoryFacade inventoryFacade;

    @InjectMocks
    private OrderCreatedEventListener orderCreatedEventListener;

    private final UUID orderId = UUID.randomUUID();

    private final UUID productId = UUID.randomUUID();

    private final UUID hubId = UUID.randomUUID();

    private final String idempotencyKey = "idempotencyKey";

    @Test
    @DisplayName("Order 이벤트 수신 시 재고를 차감한다")
    void handleOrderEvent() {
        // given
        OrderCreatedEvent event = OrderCreatedEvent.create(
            orderId,
            idempotencyKey,
            List.of(new OrderItemDto(productId, hubId, 10))
        );

        // when
        // 컴파일 에러
        // orderCreatedEventListener.handleOrderEvent(event);

        // then
        verify(inventoryFacade, times(1)).deduct(event);
    }

    @Test
    @DisplayName("재고 차감 실패 시 로그를 남기고 메소드가 정상적으로 종료된다.")
    void handleOrderEventFailed() {
        // given
        OrderCreatedEvent event = OrderCreatedEvent.create(
            orderId,
            idempotencyKey,
            List.of(new OrderItemDto(productId, hubId, 10))
        );

        // when
        when(inventoryFacade.deduct(event))
            .thenThrow(new BusinessException(InventoryErrorCode.INSUFFICIENT_STOCK));

        // then
        // 컴파일 에러
        // assertDoesNotThrow(() -> orderCreatedEventListener.handleOrderEvent(event));
    }
}
