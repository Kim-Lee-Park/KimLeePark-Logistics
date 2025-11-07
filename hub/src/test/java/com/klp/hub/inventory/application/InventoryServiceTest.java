package com.klp.hub.inventory.application;

import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID productId = UUID.randomUUID();

    private UUID hubId = UUID.randomUUID();

    private UUID inventoryId = UUID.randomUUID();

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
}
