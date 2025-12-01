package com.klp.hub.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.common.exception.ErrorCode;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.InventoryIdempotencyStatus;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent.OrderItemDto;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import com.klp.hub.inventory.infrastructure.kafka.producer.InventoryEventProducer;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.InventoryReplenishResponse.Status;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryEventProducer inventoryEventProducer;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID orderId = UUID.randomUUID();

    private UUID productId = UUID.randomUUID();

    private UUID hubId = UUID.randomUUID();

    private UUID inventoryId = UUID.randomUUID();

    String idempotencyKey = "idempotencyKey";

    @Nested
    class Read {

        @Test
        @DisplayName("한 상품의 재고를 조회할 수 있다")
        void getInventoryByProductId() {
            Inventory inventory = mock(Inventory.class);
            when(inventory.getQuantity()).thenReturn(10);
            when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

            InventoryResponse response = inventoryService.getByProductId(productId);

            assertEquals(response.productId(), productId);
            assertEquals(response.quantity(), inventory.getQuantity());
        }

        @Test
        @DisplayName("해당 상품의 재고가 없다면 예외가 발생한다")
        void throwGetInventoryByProductId() {
            when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

            ErrorCode errorCode = assertThrows(BusinessException.class,
                () -> inventoryService.getByProductId(productId))
                .getErrorCode();
            assertEquals(InventoryErrorCode.NOT_FOUND_INVENTORY, errorCode);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("재고 ID 를 통해 재고를 softDelete 할 수 있다")
        void softDelete() {
            Inventory inventory = new Inventory(productId, hubId);
            when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

            inventoryService.delete(inventoryId);

            assertTrue(inventory.isDeleted());
            assertThat(inventory.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("재고 ID 를 통해 재고를 삭제할때 재고가 없다면 예외가 발생한다")
        void throwDeletedByNullInventoryId() {
            when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

            ErrorCode errorCode = assertThrows(BusinessException.class,
                () -> inventoryService.delete(inventoryId))
                .getErrorCode();
            assertEquals(InventoryErrorCode.NOT_FOUND_INVENTORY, errorCode);
        }
    }

    @Nested
    class Create {

        @Test
        @DisplayName("재고 생성시 이미 해당 상품과 허브에 재고가 존재한다면 예외가 발생한다")
        void throwDuplicatedInventory() {
            Integer quantity = 10;
            when(inventoryRepository.save(any(Inventory.class)))
                .thenThrow(UniqueConstraintException.class);

            ErrorCode errorCode = assertThrows(BusinessException.class,
                () -> inventoryService.create(productId, hubId, quantity))
                .getErrorCode();
            assertEquals(InventoryErrorCode.INVENTORY_ALREADY_EXISTS, errorCode);
        }
    }

    @Nested
    class Deduct {

        @Test
        @DisplayName("재고 차감 요청시 이미 요청이 성공했다면 ALREADY 를 반환한다")
        void idempotency() {
            String idempotencyKey = "idempotencyKey";
            OrderCreatedEvent event = OrderCreatedEvent.create(orderId, idempotencyKey,
                List.of(new OrderItemDto(productId, hubId, 10)));

            when(inventoryRepository.acquireIdempotencyKey(idempotencyKey))
                .thenReturn(alreadyUsedIdempotency());

            InventoryDeductResponse response = inventoryService.deduct(event);

            assertEquals(InventoryDeductResponse.Status.ALREADY_DEDUCTED, response.status());
        }

        @Test
        @DisplayName("재고가 충분하고 멱등키가 처음이라면 성공을 반환하고 재고를 차감한다")
        void deduct() {
            int quantity = 5;
            OrderCreatedEvent event = OrderCreatedEvent.create(orderId, idempotencyKey,
                List.of(new OrderItemDto(productId, hubId, quantity)));

            when(inventoryRepository.acquireIdempotencyKey(idempotencyKey)).thenReturn(
                inProgressIdempotency()
            );
            when(inventoryRepository.deductAll(
                InventoryUpdatePlanner.planDeduct(event.items()))
            ).thenReturn(1);

            InventoryDeductResponse response = inventoryService.deduct(event);

            assertEquals(InventoryDeductResponse.Status.SUCCESS, response.status());
            verify(inventoryRepository, times(1)).deductAll(anyList());
        }

        @Test
        @DisplayName("재고가 부족하다면 예외를 반환하고 재고를 차감하지 않는다")
        void insufficientStock() {
            int quantity = 10;
            OrderCreatedEvent event = OrderCreatedEvent.create(orderId, idempotencyKey,
                List.of(new OrderItemDto(productId, hubId, quantity)));

            when(inventoryRepository.acquireIdempotencyKey(idempotencyKey)).thenReturn(
                inProgressIdempotency()
            );
            when(inventoryRepository.deductAll(
                InventoryUpdatePlanner.planDeduct(event.items()))
            ).thenReturn(0);

            ErrorCode errorCode = assertThrows(
                BusinessException.class, () -> inventoryService.deduct(event))
                .getErrorCode();
            assertEquals(InventoryErrorCode.INSUFFICIENT_STOCK, errorCode);
        }

    }

    @Nested
    class Replenish {

        @Test
        @DisplayName("존재하는 재고에 대해서 재고 증가요청시 성공한다")
        void replenish() {
            int quantity = 5;
            InventoryReplenishCommand command = new InventoryReplenishCommand(
                idempotencyKey,
                List.of(new InventoryReplenishCommand.Product(productId, hubId, quantity))
            );
            when(inventoryRepository.acquireIdempotencyKey(idempotencyKey)).thenReturn(
                inProgressIdempotency()
            );
            when(inventoryRepository.replenishAll(
                InventoryUpdatePlanner.planReplenish(command.products()))
            ).thenReturn(1);

            InventoryReplenishResponse response = inventoryService.replenish(command);

            assertEquals(Status.SUCCESS, response.status());
            verify(inventoryRepository, times(1)).replenishAll(anyList());
        }

        @Test
        @DisplayName("존재하지 않는 재고에 대한 재고 증가시 예외가 발생한다")
        void replenishNonExistenceInventory() {
            int quantity = 10;
            InventoryReplenishCommand command = new InventoryReplenishCommand(
                idempotencyKey,
                List.of(new InventoryReplenishCommand.Product(productId, hubId, quantity))
            );
            when(inventoryRepository.acquireIdempotencyKey(idempotencyKey)).thenReturn(
                inProgressIdempotency()
            );
            when(inventoryRepository.replenishAll(
                InventoryUpdatePlanner.planReplenish(command.products()))
            ).thenReturn(0);

            ErrorCode errorCode = assertThrows(
                BusinessException.class, () -> inventoryService.replenish(command))
                .getErrorCode();
            assertEquals(InventoryErrorCode.PARTIAL_INVENTORY_NOT_FOUND, errorCode);
        }

        @Test
        @DisplayName("재고 증가 요청시 이미 요청이 성공했다면 ALREADY 를 반환한다")
        void idempotency() {
            String idempotencyKey = "idempotencyKey";
            InventoryReplenishCommand command = new InventoryReplenishCommand(
                idempotencyKey,
                List.of(new InventoryReplenishCommand.Product(productId, hubId, 10))
            );
            when(inventoryRepository.acquireIdempotencyKey(idempotencyKey))
                .thenReturn(alreadyUsedIdempotency());

            InventoryReplenishResponse response = inventoryService.replenish(command);

            assertEquals(Status.ALREADY_REPLENISHED, response.status());
        }

    }

    private InventoryIdempotencyStatus alreadyUsedIdempotency() {
        return InventoryIdempotencyStatus.SUCCESS;
    }

    private InventoryIdempotencyStatus inProgressIdempotency() {
        return InventoryIdempotencyStatus.IN_PROGRESS;
    }
}
