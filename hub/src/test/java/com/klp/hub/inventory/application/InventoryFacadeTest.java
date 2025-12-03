package com.klp.hub.inventory.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent.OrderItemDto;
import com.klp.hub.inventory.infrastructure.lock.DistributedLockManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryFacadeTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private DistributedLockManager lockManager;

    @InjectMocks
    private InventoryFacade inventoryFacade;

    String idempotencyKey = "idempotencyKey";

    private UUID orderId = UUID.randomUUID();

    private UUID productId = UUID.randomUUID();

    private UUID hubId = UUID.randomUUID();

    @Nested
    class Deduct {

        @Test
        @DisplayName("분산락을 통한 락 획득 실패 시 예외가 발생한다")
        void lockFailed() {
            int quantity = 10;
            OrderCreatedEvent event = OrderCreatedEvent.create(orderId, idempotencyKey,
                List.of(new OrderItemDto(productId, hubId, quantity)));

            when(lockManager.tryLock(idempotencyKey)).thenReturn(false);

            assertThrows(BusinessException.class, () -> inventoryFacade.deduct(event));
        }
    }

    @Nested
    class Replenish {

        @Test
        @DisplayName("분산락을 통한 락 획득 실패 시 예외가 발생한다")
        void lockFailed() {
            int quantity = 10;
            InventoryReplenishCommand command = new InventoryReplenishCommand(
                idempotencyKey,
                List.of(new InventoryReplenishCommand.Product(productId, hubId, quantity))
            );
            when(lockManager.tryLock(idempotencyKey)).thenReturn(false);

            assertThrows(BusinessException.class, () -> inventoryFacade.replenish(command));
        }
    }
}
