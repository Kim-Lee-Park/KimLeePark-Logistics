package com.klp.hub.inventory.application;

import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.domain.repository.dto.InventoryReplenish;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
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

    public static List<InventoryDeduct> planDeduct(List<OrderCreatedEvent.OrderItem> items) {
        Map<OrderKey, Integer> aggregatedQty = aggregate(
            items,
            OrderCreatedEvent.OrderItem::productId,
            OrderCreatedEvent.OrderItem::hubId,
            OrderCreatedEvent.OrderItem::quantity
        );

        return aggregatedQty.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(ORDER))
            .map(entry -> new InventoryDeduct(
                entry.getKey().productId(),
                entry.getKey().hubId(),
                entry.getValue()))
            .toList();
    }

    public static List<InventoryReplenish> planReplenish(
        List<InventoryReplenishCommand.Product> products
    ) {
        Map<OrderKey, Integer> aggregatedQty = aggregate(
            products,
            InventoryReplenishCommand.Product::productId,
            InventoryReplenishCommand.Product::hubId,
            InventoryReplenishCommand.Product::quantity
        );

        return aggregatedQty.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(ORDER))
            .map(entry -> new InventoryReplenish(
                entry.getKey().productId(),
                entry.getKey().hubId(),
                entry.getValue()))
            .toList();
    }

    private static <T> Map<OrderKey, Integer> aggregate(
        List<T> items,
        Function<T, UUID> productId,
        Function<T, UUID> hubId,
        Function<T, Integer> quantity
    ) {
        return items.stream()
            .collect(Collectors.toMap(
                t -> new OrderKey(productId.apply(t), hubId.apply(t)),
                quantity::apply,
                Integer::sum
            ));
    }
}