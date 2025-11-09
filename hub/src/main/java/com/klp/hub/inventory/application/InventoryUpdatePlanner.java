package com.klp.hub.inventory.application;

import com.klp.hub.inventory.application.dto.InventoryDeductCommand;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand.Product;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.domain.repository.dto.InventoryReplenish;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InventoryUpdatePlanner {

    private record OrderKey(
        UUID productId,
        UUID hubId
    ) {

    }

    private static final Comparator<OrderKey> ORDER =
        Comparator.comparing((OrderKey orderKey) -> orderKey.productId)
            .thenComparing(orderKey -> orderKey.hubId);

    public static List<InventoryDeduct> planDeduct(List<InventoryDeductCommand.Product> products) {
        Map<OrderKey, Integer> aggregatedQty = products.stream()
            .collect(Collectors.toMap(
                product -> new OrderKey(product.productId(), product.hubId()),
                InventoryDeductCommand.Product::quantity,
                Integer::sum
            ));

        return aggregatedQty.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(ORDER))
            .map(entry -> new InventoryDeduct(
                entry.getKey().productId(),
                entry.getKey().hubId(),
                entry.getValue()))
            .toList();
    }

    public static List<InventoryReplenish> planReplenish(List<Product> products) {
        Map<OrderKey, Integer> aggregatedQty = products.stream()
            .collect(Collectors.toMap(
                product -> new OrderKey(product.productId(), product.hubId()),
                InventoryReplenishCommand.Product::quantity,
                Integer::sum
            ));

        return aggregatedQty.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(ORDER))
            .map(entry -> new InventoryReplenish(
                entry.getKey().productId(),
                entry.getKey().hubId(),
                entry.getValue()))
            .toList();
    }
}
