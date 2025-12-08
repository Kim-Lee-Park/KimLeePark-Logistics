package com.klp.hub.inventory.presentation.dto.request;

import com.klp.hub.inventory.application.dto.InventoryReservationCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

public record InventoryReservationRequest(
    @NotNull(message = "주문 Id는 필수입니다")
    UUID orderId,
    @NotBlank(message = "멱등키는 필수입니다")
    String idempotencyKey,
    @Valid
    @NotEmpty(message = "주문 선점 상품은 비어있을 수 없습니다.")
    List<ReservationItemRequest> items
) {

    public record ReservationItemRequest(
        @NotNull(message = "상품 Id는 필수입니다.")
        UUID productId,
        @NotNull(message = "담당 허브 Id는 필수입니다")
        UUID hubId,
        @NotNull(message = "수량은 필수입니다")
        @Positive(message = "수량은 0 혹은 음수일 수 없습니다")
        Integer quantity
    ) {

    }

    public InventoryReservationCommand toCommand() {
        return new InventoryReservationCommand(
            orderId,
            idempotencyKey,
            items.stream()
                .map(item -> new InventoryReservationCommand.ReservationItem(
                    item.productId(),
                    item.hubId(),
                    item.quantity()
                ))
                .toList()
        );
    }
}
