package com.klp.hub.inventory.presentation.dto.request;

import com.klp.hub.inventory.application.dto.InventoryDeductCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record InventoryDeductRequest(
    @NotBlank(message = "멱등키는 필수 값입니다.")
    String idempotencyKey,

    @NotNull(message = "상품 목록은 필수 값입니다.")
    @NotEmpty(message = "상품 목록은 최소 1개 이상이어야 합니다.")
    @Valid
    List<Product> products
) {

    public InventoryDeductCommand toCommand() {
        return new InventoryDeductCommand(
            idempotencyKey,
            products.stream().map(product -> new InventoryDeductCommand.Product(
                product.productId,
                product.hubId,
                product.quantity
            )).toList()
        );
    }

    public record Product(
        @NotNull(message = "상품 ID는 필수 값입니다.")
        UUID productId,
        @NotNull(message = "허브 ID는 필수 값입니다.")
        UUID hubId,
        @NotNull(message = "수량은 필수 값입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        Integer quantity
    ) {

    }
}
