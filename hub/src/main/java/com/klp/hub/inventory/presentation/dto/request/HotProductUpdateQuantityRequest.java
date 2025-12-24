package com.klp.hub.inventory.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record HotProductUpdateQuantityRequest(
    @NotNull(message = "상품 ID는 필수 값입니다.")
    UUID productId,
    @NotNull(message = "허브 ID는 필수 값입니다.")
    UUID hubId,
    @NotNull(message = "수량은 필수 값입니다.")
    @Min(value = 0, message = "수량은 0 이상이어야 합니다.")
    Integer quantity
) {
}
