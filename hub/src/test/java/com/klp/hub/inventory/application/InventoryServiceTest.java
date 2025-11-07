package com.klp.hub.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.hub.inventory.application.dto.InventoryDeductCommand;
import com.klp.hub.inventory.application.dto.InventoryDeductCommand.Product;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse.Process;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID productId = UUID.randomUUID();

    private UUID hubId = UUID.randomUUID();

    private UUID inventoryId = UUID.randomUUID();

    String idempotencyKey = "idempotencyKey";

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

        assertThrows(RuntimeException.class, () -> inventoryService.getByProductId(productId));
    }

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

        assertThrows(RuntimeException.class, () -> inventoryService.delete(inventoryId));
    }

    @Test
    @DisplayName("재고 차감 요청시 이미 처리된 요청이라면 ALREADY 를 반환한다")
    void idempotency() {
        String idempotencyKey = "idempotencyKey";
        InventoryDeductCommand command = new InventoryDeductCommand(
            idempotencyKey,
            List.of(new Product(productId, hubId, 10))
        );
        when(inventoryRepository.tryAcquireIdempotencyKey(idempotencyKey))
            .thenReturn(false);

        InventoryDeductResponse response = inventoryService.deduct(command);

        assertEquals(Process.ALREADY_DEDUCTED, response.process());
    }

    @Test
    @DisplayName("재고가 충분하고 멱등키가 처음이라면 성공을 반환하고 재고를 차감한다")
    void deduct() {
        int quantity = 5;
        InventoryDeductCommand command = new InventoryDeductCommand(
            idempotencyKey,
            List.of(new Product(productId, hubId, quantity))
        );
        when(inventoryRepository.tryAcquireIdempotencyKey(idempotencyKey)).thenReturn(true);
        when(inventoryRepository.deductAll(command.toInventoryDeductList())).thenReturn(1);

        InventoryDeductResponse response = inventoryService.deduct(command);

        assertEquals(Process.SUCCESS, response.process());
        verify(inventoryRepository, times(1)).deductAll(anyList());
    }
}
