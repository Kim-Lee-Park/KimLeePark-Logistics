package com.klp.hub.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.klp.hub.inventory.application.dto.InventoryDeductCommand;
import com.klp.hub.inventory.application.dto.InventoryDeductCommand.Product;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InventoryUpdatePlannerTest {

    private static final UUID P1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID P2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID H1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID H2 = UUID.fromString("10000000-0000-0000-0000-000000000002");

    @Nested
    class Deduct {

        @Test
        @DisplayName("동일한 키는 합산되고 productId 와 hubId 순서로 오름차순 정렬된다")
        void order() {
            List<Product> products = List.of(
                deductProduct(P2, H1, 1),
                deductProduct(P1, H2, 1),
                deductProduct(P1, H2, 1),
                deductProduct(P1, H1, 1)
            );

            List<InventoryDeduct> deducts = InventoryUpdatePlanner.planDeduct(products);

            assertThat(deducts)
                .extracting(
                    InventoryDeduct::productId,
                    InventoryDeduct::hubId,
                    InventoryDeduct::quantity
                )
                .containsExactly(
                    tuple(P1, H1, 1),
                    tuple(P1, H2, 2),
                    tuple(P2, H1, 1)
                );
        }

        private InventoryDeductCommand.Product deductProduct(UUID productId, UUID hubId,
            int quantity) {
            return new InventoryDeductCommand.Product(productId, hubId, quantity);
        }
    }
}
